package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.games.GamesClient;
import butterknife.InjectView;
import butterknife.Views;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.games.GamesClient;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.DrawerAdapter;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.view.ActionBarDrawerToggleCompat;
import org.joda.time.DateTime;

public abstract class GdgNavDrawerActivity extends GdgActivity {

    @InjectView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.left_drawer)
    ListView mDrawerContent;

    protected DrawerAdapter mDrawerAdapter;

    protected ActionBarDrawerToggleCompat mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);

        initNavigationDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initNavigationDrawer() {
        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerContent.setAdapter(mDrawerAdapter);
        mDrawerContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(i);

                switch (item.getId()) {
                    case Const.DRAWER_ACHIEVEMENTS:
                        if (mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                            getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                                @Override
                                public void onGotGamesClient(GamesClient c) {
                                    startActivityForResult(c.getAchievementsIntent(), 0);
                                }
                            });
                        } else {
                            Crouton.makeText(GdgNavDrawerActivity.this, getString(R.string.achievements_need_signin), Style.INFO).show();
                        }
                        break;
                    case Const.DRAWER_HOME:
                        navigateTo(MainActivity.class, null);
                        break;
                    case Const.DRAWER_GDL:
                        navigateTo(GdlActivity.class, null);
                        break;
                    case Const.DRAWER_SPECIAL:
                        Bundle special = new Bundle();
                        special.putInt(Const.SPECIAL_EVENT_LOGO_EXTRA, R.drawable.flightschool);
                        special.putString(Const.SPECIAL_EVENT_VIEWTAG_EXTRA, "dartflightschool");
                        special.putString(Const.SPECIAL_EVENT_CACHEKEY_EXTRA, "dartflightschool");
                        special.putLong(Const.SPECIAL_EVENT_START_EXTRA, DateTime.now().getMillis());
                        special.putLong(Const.SPECIAL_EVENT_END_EXTRA, 1419984000000L);
                        special.putInt(Const.SPECIAL_EVENT_DESCRIPTION_EXTRA, R.string.flightschool_description);
                        navigateTo(SpecialEventActivity.class, special);
                        break;
                    case Const.DRAWER_PULSE:
                        navigateTo(PulseActivity.class, null);
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggleCompat(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                if (mPreferences.getBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT)) {
                    mPreferences.edit().putBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, !Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT).apply();
                }
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void navigateTo(Class<? extends GdgActivity> activityClass, Bundle additional) {
        Intent i = new Intent(GdgNavDrawerActivity.this, activityClass);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(additional != null)
            i.putExtras(additional);

        startActivity(i);
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPreferences.getBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }
}
