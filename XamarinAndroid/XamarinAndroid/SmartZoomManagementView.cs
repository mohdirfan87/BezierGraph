
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Util;
using Android.Views;
using Android.Widget;
using XamarinAndroid.Views;

namespace XamarinAndroid
{
    public class SmartZoomManagementView : LinearLayout
    {
        public Action<object, RelativeLayout> managementViewAction;
        private float density;
        private Context context;
        public SmartZoomManagementView(Context context) :
            base(context)
        {
            this.context = context;
            density = Resources.DisplayMetrics.Density;
            Initialize(context);
        }

        public SmartZoomManagementView(Context context, IAttributeSet attrs) :
            base(context, attrs)
        {
            Initialize(context);
        }

        public SmartZoomManagementView(Context context, IAttributeSet attrs, int defStyle) :
            base(context, attrs, defStyle)
        {
            Initialize(context);
        }

        void Initialize(Context context)
        {
            Orientation = Orientation.Vertical;
            LinearLayout.LayoutParams rootViewParams = new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.MatchParent, LinearLayout.LayoutParams.WrapContent);
            LayoutParameters = rootViewParams;
            for (int i = 0; i < 5; i++)
            {
                RelativeLayout main = new RelativeLayout(context);
                main.Tag = i;
                main.SetPadding(0, 0, 0, (int)(10 * density));

                Box background = new Box(context);
                LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(
                   LinearLayout.LayoutParams.WrapContent, LinearLayout.LayoutParams.WrapContent);
                backgroundParams.Gravity = GravityFlags.Center;
                background.LayoutParameters = backgroundParams;
                main.AddView(background);


                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MatchParent, 0, 1.0f);
                if (i == 0) { layoutParams.TopMargin = (int)(10 * density); }


                main.LayoutParameters = layoutParams;
                TextView textViewTitle = new TextView(context)
                {
                    Text = "ESC",
                    Id = View.GenerateViewId()
                };
                textViewTitle.SetPadding(0, (int)(2 * density), 0, 0);
                RelativeLayout.LayoutParams textViewTitleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WrapContent, RelativeLayout.LayoutParams.WrapContent);
                textViewTitleParams.AddRule(LayoutRules.CenterHorizontal);
                textViewTitle.LayoutParameters = textViewTitleParams;
                main.AddView(textViewTitle);

                TextView textViewValue = new TextView(context)
                {
                    Text = "5.0"
                };
                textViewValue.SetPadding(0, (int)(2 * density), 0, 0);
                RelativeLayout.LayoutParams textViewValueParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WrapContent, RelativeLayout.LayoutParams.WrapContent);
                textViewValueParams.AddRule(LayoutRules.CenterHorizontal);
                textViewValueParams.AddRule(LayoutRules.Below, textViewTitle.Id);
                textViewValue.LayoutParameters = textViewValueParams;
                main.AddView(textViewValue);

                main.Click += (sender, args) =>
                {
                    managementViewAction.Invoke(main.Tag, main);

                };
                AddView(main);
            }
        }
    }
}
