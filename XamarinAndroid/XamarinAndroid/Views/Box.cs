using System;
using Android.Content;
using Android.Graphics;
using Android.Runtime;
using Android.Util;
using Android.Views;

namespace XamarinAndroid.Views
{
    [Register("XamarinAndroid.Views.Box")]
    public class Box : View
    {
        float density = 1.0f;
        private int width { set; get; }
        private int height { set; get; }
        private float lineX;
        private float lineY;

        private Paint mPaint = new Paint
        {
            Color = Color.Rgb(30, 44, 53),
            AntiAlias = true,
            StrokeWidth = 4f
        };
        private RectF box = new RectF();
        private RectF outerRect = new RectF();

        public Box(Context context) : base(context)
        {
        }
        public Box(Context context, IAttributeSet attrs) : base(context, attrs)
        {

        }
        protected override void OnDraw(Canvas canvas)
        {
            base.OnDraw(canvas);

            initPoints();
            outerBox(canvas);
            innerBox(canvas);
          

           
        }
        private void initPoints()
        {
            density = Resources.DisplayMetrics.Density;
            Log.Info("Density ", "density " + density);
            //size = width;
            Log.Info("initPoints ", "width " + width + "height " + height);
            outerRect.Left = 0f;
            outerRect.Top = 0f;
            outerRect.Right = width;
            outerRect.Bottom = height;

            box.Left = outerRect.Left + 3 * density;
            box.Top = outerRect.Top + 3 * density;
            box.Right = outerRect.Right - 3 * density;
            box.Bottom = outerRect.Bottom - 3 * density;
            

            lineX = box.Left;
            lineY = box.Top + (box.Height() * .70f);
            Log.Info("MyView", "MyView ");
           

         
        }
        private void innerBox(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(35, 50, 61);
            mPaint.AntiAlias = true;
            //mPaint.StrokeWidth = 5 * density;
            mPaint.SetStyle(Paint.Style.Fill);

            canvas.DrawRoundRect(box, 10.0f, 10.0f, mPaint);
            drawPath(canvas);
        }
        private void drawPath(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(255, 182, 66);
            mPaint.AntiAlias = true;
            //mPaint.StrokeWidth =5* density;
            mPaint.SetStyle(Paint.Style.Fill);
            Path tPath = new Path();
            tPath.MoveTo(box.Right * .90f, box.Bottom);
            tPath.LineTo(box.Right, box.Bottom -(box.Right-box.Right*.90f));
            tPath.LineTo(box.Right, box.Bottom);
            tPath.LineTo(box.Right * .90f, box.Bottom);
            tPath.Close();
            canvas.DrawPath(tPath, mPaint);

        }
        private void outerBox(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(255, 182, 66);
            mPaint.AntiAlias = true;
            mPaint.SetStyle(Paint.Style.Fill);
            canvas.DrawRoundRect(outerRect, 10.0f, 10.0f, mPaint);
        }
        protected override void OnMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            base.OnMeasure(widthMeasureSpec, heightMeasureSpec);
            MeasureSpecMode mode = MeasureSpec.GetMode(widthMeasureSpec);
            this.width = MeasureSpec.GetSize(widthMeasureSpec);
            this.height = MeasureSpec.GetSize(heightMeasureSpec);



            Log.Info("MeasureSpecMode", "width " + width);
            Log.Info("MeasureSpecMode", "height " + height);
            switch (mode)
            {
                case MeasureSpecMode.Exactly:
                    Log.Info("MeasureSpecMode", "Exactly " + mode);
                    break;
                case MeasureSpecMode.AtMost:
                    Log.Info("MeasureSpecMode", "AtMost " + mode);
                    break;
                case MeasureSpecMode.Unspecified:
                    Log.Info("MeasureSpecMode", "Unspecified " + mode);
                    break;
            }
        }
        public float dpToPx(float dp)
        {
            return (float)Math.Round((float)dp * density);
        }
    }

}
