using System;
using System.Collections.Generic;
using System.Net;

using Android.App;
using Android.Content;
using Android.Graphics;
using Android.OS;
using Android.Runtime;
using Android.Support.Design.Widget;
using Android.Support.V4.Content;
using Android.Support.V7.App;
using Android.Util;
using Android.Views;
using Android.Widget;
using Java.Lang;
using Java.Nio;
using XamarinAndroid.Views;
using Object = Java.Lang.Object;

namespace XamarinAndroid
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
    {
        private float density;
        private RelativeLayout RootView;
        private LinearLayout Panel_00;
        private LinearLayout BatteryStripLayout;
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Xamarin.Essentials.Platform.Init(this, savedInstanceState);
            SetContentView(Resource.Layout.activity_main);

            density = Resources.DisplayMetrics.Density;
            Typeface typeFace = Typeface.CreateFromAsset(Application.Context.Assets, "fonts/GalaxiePolarisBook.otf");

            RootView = FindViewById<RelativeLayout>(Resource.Id.RootView);
            TextView battery_title_tv = FindViewById<TextView>(Resource.Id.battery_title_tv);
            battery_title_tv.SetTypeface(typeFace, TypefaceStyle.Normal);
            Panel_00 = FindViewById<LinearLayout>(Resource.Id.Panel_00);
            Panel_00.Click += (sender, args) =>
            {
                inflateView();
            };

            BatteryStripLayout = FindViewById<LinearLayout>(Resource.Id.BatteryStripLayout);
            GetBatteryView();

            
            Handler h = new Handler();
            Action myAction = () =>
            {
                
                Log.Info("BatteryStripLayout", ""+ BatteryStripLayout.ChildCount);
                UpdateButtChargeStatus(50);
            };

            h.PostDelayed(myAction, 5000);
            //h.Post(myAction);
        }
        private void UpdateButtChargeStatus(int percent)
        {
            TextView tmpTextView = BatteryStripLayout.GetChildAt(0) as TextView;
            tmpTextView.Text=percent.ToString();
            int GrayStrips = 10 - (percent / 10);
            for (int i = 0; i < 10; i++)
            {
                LinearLayout tmpLayout = BatteryStripLayout.GetChildAt(i+1) as LinearLayout;
                if (i < GrayStrips)
                {
                    //tmpLayout.SetBackgroundColor(Android.Graphics.Color.ParseColor("#68747A"));
                    tmpLayout.Background = ContextCompat.GetDrawable(this, Resource.Drawable.empty_battery_bg);
                }
                else
                {
                    //tmpLayout.SetBackgroundColor(Android.Graphics.Color.ParseColor("#21B359"));
                    tmpLayout.Background = ContextCompat.GetDrawable(this, Resource.Drawable.battery_bg);
                }
            }
        }
        private void GetBatteryView()
        {
            for (int i = 0; i < 10; i++)
            {
                LinearLayout batteryStripLinearLayout = new LinearLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MatchParent,
                        (int)Resources.GetDimension(Resource.Dimension.battery_height)
                );
                batteryStripLinearLayout.Background = ContextCompat.GetDrawable(this, Resource.Drawable.battery_bg);
                // batteryStripLinearLayout.SetBackgroundColor(Android.Graphics.Color.ParseColor("#68747A"));
                layoutParams.TopMargin = (int)Resources.GetDimension(Resource.Dimension.battery_margin_top);
                batteryStripLinearLayout.LayoutParameters = layoutParams;
                BatteryStripLayout.AddView(batteryStripLinearLayout);
            }
        }

        //private void GetESCManagementView(LinearLayout management_view)
        //{
        //    LinearLayout rootView = new LinearLayout(this);
        //    rootView.Orientation = Orientation.Vertical;
        //    LinearLayout.LayoutParams rootViewParams = new LinearLayout.LayoutParams(
        //          LinearLayout.LayoutParams.MatchParent, LinearLayout.LayoutParams.WrapContent);
        //    rootView.LayoutParameters = rootViewParams;

        //    for (int i = 0; i < 5; i++)
        //    {
        //        RelativeLayout main = new RelativeLayout(this);
        //        main.SetPadding(0, 0, 0, (int)(10 * density));

        //        Box background = new Box(this);
        //        LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
        //           LinearLayout.LayoutParams.WrapContent, LinearLayout.LayoutParams.WrapContent);
        //        backgroundParams.Gravity = GravityFlags.Center;
        //        background.LayoutParameters = backgroundParams;
        //        main.AddView(background);

               
        //        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        //            LinearLayout.LayoutParams.MatchParent,0,1.0f);
        //        if (i == 0){layoutParams.TopMargin = (int)(10 * density); }
                
                
        //        main.LayoutParameters = layoutParams;
        //        TextView textViewTitle = new TextView(this)
        //        {
        //            Text="ESC",
        //            Id = View.GenerateViewId()
        //        };
        //        textViewTitle.SetPadding(0, (int)(2 * density), 0, 0);
        //        RelativeLayout.LayoutParams textViewTitleParams = new RelativeLayout.LayoutParams(
        //            RelativeLayout.LayoutParams.WrapContent, RelativeLayout.LayoutParams.WrapContent);
        //        textViewTitleParams.AddRule(LayoutRules.CenterHorizontal);
        //        textViewTitle.LayoutParameters = textViewTitleParams;
        //        main.AddView(textViewTitle);

        //        TextView textViewValue = new TextView(this)
        //        {
        //            Text = "5.0"
        //        };
        //        textViewValue.SetPadding(0, (int)(2 * density), 0, 0);
        //        RelativeLayout.LayoutParams textViewValueParams = new RelativeLayout.LayoutParams(
        //            RelativeLayout.LayoutParams.WrapContent, RelativeLayout.LayoutParams.WrapContent);
        //        textViewValueParams.AddRule(LayoutRules.CenterHorizontal);
        //        textViewValueParams.AddRule(LayoutRules.Below, textViewTitle.Id);
        //        textViewValue.LayoutParameters = textViewValueParams;
        //        main.AddView(textViewValue);

        //        main.Click += (sender, args) =>
        //        {
        //            Toast.MakeText(this, "clicked item " + i, ToastLength.Short).Show();
        //        };
        //        rootView.AddView(main);
        //    }
        //    management_view.AddView(rootView);
        //}
        private void inflateView()
        {
            //var typeface = Typeface.CreateFromAsset(Assets, "fonts/GalaxiePolarisBook.otf");
            var inflater = GetSystemService(Context.LayoutInflaterService) as LayoutInflater;
            
            LinearLayout ZoomView = inflater.Inflate(Resource.Layout.SecondView, null) as LinearLayout;
            MyView ZoomWidgets = ZoomView.FindViewById<MyView>(Resource.Id.ZoomWidgets);
            LinearLayout management_view = ZoomView.FindViewById<LinearLayout>(Resource.Id.management_view);

            ZoomWidgets.IsZoom = true;
            RootView.AddView(ZoomView);

            //ManagementView managementView = new ManagementView(this);
            //managementView.managementViewAction += (sender, mainLayout) =>
            //{
            //    Toast.MakeText(this, "clicked item " + int.Parse(string.Format("{0}", sender)), ToastLength.Short).Show();
            //};
            //management_view.AddView(managementView.GetESCManagementView());
            //GetESCManagementView(management_view);
            SmartZoomManagementView managementView = new SmartZoomManagementView(this);
            managementView.managementViewAction += (sender, mainLayout) =>
            {
                Toast.MakeText(this, "clicked item " + int.Parse(string.Format("{0}", sender)), ToastLength.Short).Show();
            };
            management_view.AddView(managementView);

        }



        public override bool OnCreateOptionsMenu(IMenu menu)
        {
            MenuInflater.Inflate(Resource.Menu.menu_main, menu);
            return true;
        }

        public override bool OnOptionsItemSelected(IMenuItem item)
        {
            int id = item.ItemId;
            if (id == Resource.Id.action_settings)
            {
                return true;
            }

            return base.OnOptionsItemSelected(item);
        }


        public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Android.Content.PM.Permission[] grantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);

            base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

