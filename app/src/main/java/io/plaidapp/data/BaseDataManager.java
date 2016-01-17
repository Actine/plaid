/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.data;

import android.content.Context;
import com.google.gson.GsonBuilder;
import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.AuthInterceptor;
import io.plaidapp.data.api.ClientAuthInterceptor;
import io.plaidapp.data.api.designernews.DesignerNewsService;
import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.producthunt.ProductHuntService;
import io.plaidapp.data.prefs.DesignerNewsPrefs;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.util.StethoIntegratedClientFactory;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for loading data.
 */
public abstract class BaseDataManager implements
        DataLoadingSubject,
        DribbblePrefs.DribbbleLoginStatusListener,
        DesignerNewsPrefs.DesignerNewsLoginStatusListener{

    private DesignerNewsPrefs designerNewsPrefs;
    private DesignerNewsService designerNewsApi;
    private DribbblePrefs dribbblePrefs;
    private DribbbleService dribbbleApi;
    private ProductHuntService productHuntApi;
    private AtomicInteger loadingCount;
    private List<DataLoadingSubject.DataLoadingCallbacks> loadingCallbacks;

    public BaseDataManager(Context context) {
        // setup the API access objects
        designerNewsPrefs = DesignerNewsPrefs.get(context);
        createDesignerNewsApi();
        dribbblePrefs = DribbblePrefs.get(context);
        createDribbbleApi();
        createProductHuntApi();
        loadingCount = new AtomicInteger(0);
    }

    public abstract void onDataLoaded(List<? extends PlaidItem> data);

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    protected void loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            notifyCallbacksLoadingStarted();
        }
    }

    protected void loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            notifyCallbacksLoadingFinished();
        }
    }

    protected void resetLoadingCount() {
        loadingCount.set(0);
    }

    protected static void setPage(List<? extends PlaidItem> items, int page) {
        for (PlaidItem item : items) {
            item.page = page;
        }
    }

    protected static void setDataSource(List<? extends PlaidItem> items, String dataSource) {
        for (PlaidItem item : items) {
            item.dataSource = dataSource;
        }
    }

    private void createDesignerNewsApi() {
        designerNewsApi = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .setClient(StethoIntegratedClientFactory.makeClientForRetrofit())
                .setRequestInterceptor(new ClientAuthInterceptor(designerNewsPrefs.getAccessToken(),
                        BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .build()
                .create(DesignerNewsService.class);
    }

    public DesignerNewsService getDesignerNewsApi() {
        return designerNewsApi;
    }

    public DesignerNewsPrefs getDesignerNewsPrefs() {
        return designerNewsPrefs;
    }

    private void createDribbbleApi() {
        dribbbleApi = new RestAdapter.Builder()
                .setClient(StethoIntegratedClientFactory.makeClientForRetrofit())
                .setEndpoint(DribbbleService.ENDPOINT)
                .setConverter(new GsonConverter(new GsonBuilder()
                        .setDateFormat(DribbbleService.DATE_FORMAT)
                        .create()))
                .setRequestInterceptor(new AuthInterceptor(dribbblePrefs.getAccessToken()))
                .build()
                .create((DribbbleService.class));
    }

    public DribbbleService getDribbbleApi() {
        return dribbbleApi;
    }

    public DribbblePrefs getDribbblePrefs() {
        return dribbblePrefs;
    }

    private void createProductHuntApi() {
        productHuntApi = new RestAdapter.Builder()
                .setClient(StethoIntegratedClientFactory.makeClientForRetrofit())
                .setEndpoint(ProductHuntService.ENDPOINT)
                .setRequestInterceptor(
                        new AuthInterceptor(BuildConfig.PROCUCT_HUNT_DEVELOPER_TOKEN))
                .build()
                .create(ProductHuntService.class);
    }

    public ProductHuntService getProductHuntApi() {
        return productHuntApi;
    }

    @Override
    public void addCallbacks(DataLoadingSubject.DataLoadingCallbacks callbacks) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }
        loadingCallbacks.add(callbacks);
    }

    @Override
    public void removeCallbacks(DataLoadingSubject.DataLoadingCallbacks callbacks) {
        if (loadingCallbacks.contains(callbacks)) {
            loadingCallbacks.remove(callbacks);
        }
    }

    protected void notifyCallbacksLoadingStarted() {
        if (loadingCallbacks == null) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataStartedLoading();
        }
    }

    protected void notifyCallbacksLoadingFinished() {
        if (loadingCallbacks == null) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataFinishedLoading();
        }
    }

    @Override
    public void onDribbbleLogin() {
        createDribbbleApi(); // capture the auth token
    }

    @Override
    public void onDribbbleLogout() {
        createDribbbleApi(); // clear the auth token
    }

    @Override
    public void onDesignerNewsLogin() {
        createDesignerNewsApi(); // capture the auth token
    }

    @Override
    public void onDesignerNewsLogout() {
        createDesignerNewsApi(); // clear the auth token
    }

}
