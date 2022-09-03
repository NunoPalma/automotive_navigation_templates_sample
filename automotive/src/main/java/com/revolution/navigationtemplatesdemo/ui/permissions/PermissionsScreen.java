package com.revolution.navigationtemplatesdemo.ui.permissions;

/**
 * Created by Nuno Palma on 31-08-2022
 **/

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.ParkedOnlyOnClickListener;
import androidx.car.app.model.Template;

import com.revolution.navigationtemplatesdemo.R;
import com.revolution.navigationtemplatesdemo.ui.menu.MenuScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for asking the user to grant location permission.
 */
public class PermissionsScreen extends Screen {

    public PermissionsScreen(
            @NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        List<String> permissions = new ArrayList<>();
        permissions.add(ACCESS_FINE_LOCATION);

        OnClickListener listener = ParkedOnlyOnClickListener.create(() ->
                getCarContext().requestPermissions(
                        permissions,
                        (approved, rejected) -> {
                            CarToast.makeText(
                                    getCarContext(),
                                    String.format("Approved: %s Rejected: %s", approved, rejected),
                                    CarToast.LENGTH_LONG).show();
                            if (!approved.isEmpty()) {
                                getScreenManager().push(
                                        new MenuScreen(getCarContext()));
                                finish();
                            }
                        }));

        Action action = new Action.Builder()
                .setTitle(getCarContext().getString(R.string.grant_permission))
                .setBackgroundColor(CarColor.GREEN)
                .setOnClickListener(listener)
                .build();

        return new MessageTemplate.Builder(getCarContext().getString(R.string.permission_request_message))
                .addAction(action).setHeaderAction(
                        Action.APP_ICON).build();
    }
}