package de.czyrux.store.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import de.czyrux.store.R;
import de.czyrux.store.core.domain.cart.CartProduct;
import de.czyrux.store.core.domain.cart.CartStore;
import de.czyrux.store.inject.Injector;
import de.czyrux.store.ui.base.BaseActivity;
import de.czyrux.store.ui.cart.CartFragment;
import de.czyrux.store.ui.catalog.CatalogFragment;
import de.czyrux.store.ui.util.PlaceholderFragment;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroceryStoreActivity extends BaseActivity {

    private static final int TABS_COUNT = 2;
    private static final int CATALOG_TAB_POSITION = 0;
    private static final int CART_TAB_POSITION = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.grocery_store_viewpager)
    ViewPager viewPager;

    @BindView(R.id.grocery_store_tabs)
    TabLayout tabLayout;

    private CartStore cartStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        setupDependencies();
    }

    @Override
    protected int layoutId() {
        return R.layout.grocery_store_activity;
    }

    @SuppressWarnings("ConstantConditions")
    private void setupViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter tabsSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.grocery_store_viewpager);
        viewPager.setAdapter(tabsSectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // empty
            }

            @Override
            public void onPageSelected(int position) {
                trackScreenName();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // empty
            }
        });
        tabLayout = (TabLayout) findViewById(R.id.grocery_store_tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(CART_TAB_POSITION).setText("Cart");
        tabLayout.getTabAt(CATALOG_TAB_POSITION).setText("Catalog");
    }

    @Override
    protected void setupDependencies() {
        super.setupDependencies();
        cartStore = Injector.cartStore();
    }


    @Override
    protected String getScreenName() {
        return getCurrentTitle();
    }

    /**
     * Return the title of the currently displayed section.
     *
     * @return title of the section
     */
    private String getCurrentTitle() {
        int position = viewPager.getCurrentItem();
        switch (position) {
            case 0:
                return "ProductList";
            case 1:
                return "Cart";
            default:
                return "Undefined";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        addSubscription(cartStore.observe()
                .subscribeOn(Schedulers.computation())
                .map(cart -> {
                    int cartProductsCount = 0;
                    for (CartProduct product : cart.products) {
                        cartProductsCount += product.quantity;
                    }
                    return cartProductsCount;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateCartTabTitle));
    }


    @SuppressWarnings("ConstantConditions")
    private void updateCartTabTitle(int count) {
        tabLayout.getTabAt(CART_TAB_POSITION).setText("Cart (" + count + ")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grocery_store, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == CATALOG_TAB_POSITION) {
                return CatalogFragment.newInstance();
            } else if (position == CART_TAB_POSITION) {
                return CartFragment.newInstance();
            }

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return TABS_COUNT;
        }


    }
}
