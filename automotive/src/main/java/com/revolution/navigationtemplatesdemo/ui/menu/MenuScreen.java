package com.revolution.navigationtemplatesdemo.ui.menu;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Template;

import com.revolution.navigationtemplatesdemo.R;
import com.revolution.navigationtemplatesdemo.ui.tracks.TracksScreen;

/**
 * Created by Nuno Palma on 02-09-2022
 **/
public class MenuScreen extends Screen {

    public MenuScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Action trackHistoryAction = new Action.Builder()
                .setTitle(getCarContext().getString(R.string.track_history))
                .setBackgroundColor(CarColor.BLUE)
                .setOnClickListener(() -> getScreenManager().push(
                        new TracksScreen(getCarContext())))
                .build();

        Action loadTrackHistoryAction = new Action.Builder()
                .setTitle(getCarContext().getString(R.string.load_track))
                .setOnClickListener(() -> {
                    CarToast.makeText(getCarContext(), "Unavailable.", CarToast.LENGTH_SHORT)
                            .show();
                })
                .setBackgroundColor(CarColor.BLUE)
                .build();

        return new MessageTemplate.Builder(getCarContext().getString(R.string.demo))
                .addAction(trackHistoryAction)
                .addAction(loadTrackHistoryAction)
                .setHeaderAction(Action.APP_ICON)
                .build();
    }
}
