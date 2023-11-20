package us.zoom.sdkexample2;

import us.zoom.sdk.NewMeetingActivity;

public class MyNewMeetingActivity extends NewMeetingActivity {
    @Override
    protected int getLayout() {
        return R.layout.my_new_meeting_layout;
    }

    @Override
    protected int getLayoutForTablet() {
        return R.layout.my_new_meeting_tablet_layout;
    }

    @Override
    protected boolean isSensorOrientationEnabled() {
        return false;
    }
}
