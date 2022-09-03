package com.revolution.navigationtemplatesdemo.core.car;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

/**
 * Created by Nuno Palma on 30-08-2022
 **/
public class CoreCarAppService extends CarAppService {

    /**
     * HostValidator.ALLOW_ALL_HOSTS_VALIDATOR should not be used in production builds.
     */
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @Override
    @NonNull
    public Session onCreateSession() {
        return new CoreSession();
    }
}
