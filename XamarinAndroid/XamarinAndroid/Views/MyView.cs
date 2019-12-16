using System;
using System.Linq;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Runtime;
using Android.Util;
using Android.Views;

namespace XamarinAndroid.Views
{
    [Register("XamarinAndroid.Views.MyView")]
    public class MyView : View
    {
        private float lineX;
        private float lineY;
        Path mCurve = new Path();

        float[] xPoints = { 10, 40, 70, 100, 130, 160, 190, 210, 240, 270 };
        float[] yPoints = { 10000, 30000, 60000, 80000, 90000, 0, 10000, 15000, 40000, 9000 };

        private int width { set; get; }
        private int height { set; get; }

        private bool _isZoom;
        public bool IsZoom
        {
            get { return _isZoom; }
            set { _isZoom = value; RequestLayout(); }
        }
        private Paint mPaint = new Paint
        {
            Color = Color.Rgb(30, 44, 53),
            AntiAlias = true,
            StrokeWidth = 4f
        };
        private RectF box = new RectF();
        private RectF outerRect = new RectF();

        PointF[] points;
        PointF[] pointsCon1;
        PointF[] pointsCon2;

        float density = 1.0f;
        private void cordinateX()
        {
            float maxX = (float)Math.Round((box.Width() / yPoints.Length));
            if (xPoints.Length != yPoints.Length)
            {
                xPoints = new float[yPoints.Length];
            }
            Log.Debug("cordinateX", "maxX: " + box.Width());
            float cordX = 0;
            for (int i = 0; i < yPoints.Length; i++)
            {
                cordX += maxX;
                xPoints[i] = cordX;
            }
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
            cordinateX();

            lineX = box.Left;
            lineY = box.Top + (box.Height() * .70f);
            Log.Info("MyView", "MyView ");
            points = new PointF[xPoints.Length + 3 + 1];
            pointsCon1 = new PointF[points.Length];
            pointsCon2 = new PointF[points.Length];
            points = points.Select(instance => new PointF()).ToArray();
            pointsCon1 = pointsCon1.Select(instance => new PointF()).ToArray();
            pointsCon2 = pointsCon2.Select(instance => new PointF()).ToArray();

            initControls();
            float graphLimit = box.Bottom * .30f;
            float maxValue = yPoints.Max() * density;
            if (maxValue > graphLimit)
            {
                yPoints = yPoints.Select(n => n * (1 - ((maxValue - graphLimit) / maxValue))).ToArray();
                Array.Clear(points, 0, points.Length);
                points = points.Select(instance => new PointF()).ToArray();
                initControls();
            }
            for (int i = 1; i < points.Length; i++)
            {
                pointsCon1[i].Set((points[i].X + points[i - 1].X) / 2, points[i - 1].Y);
                pointsCon2[i].Set((points[i].X + points[i - 1].X) / 2, points[i].Y);
            }
        }
        private void initControls()
        {
            //yPoints=yPoints.Take(3).Select(item=>box.Left);
            //var keys = (from s in context.Keys
            //            where !badCodes.Contains(s.Code)
            //            orderby s.Name
            //            select s).ToList<Keys>();
            //points = (from point in points
            //          where points.Length < 3

            //          select point).ToArray();

            //points = points.Select(point => point.Set(box.Left + 0, lineY - 0)).ToArray();

            for (int i = 0; i < 3; i++)
            {
                points[i].Set(box.Left + 0, lineY - 0);
            }
            points[3].Set(box.Left + 0, lineY - dpToPx(yPoints[0]));
            for (int i = 4; i < points.Length - 1; i++)
            {
                points[i].Set(box.Left + xPoints[i - 3], lineY - dpToPx(yPoints[i - 3]));
            }
            points[points.Length - 1].Set(box.Left + xPoints[xPoints.Length - 1], lineY - 0);

        }
        Typeface typeFace;
        public MyView(Context context, IAttributeSet attrs) : base(context, attrs)
        {
            typeFace = Typeface.CreateFromAsset(Application.Context.Assets, "fonts/GalaxiePolarisBook.otf");
        }
        protected override void OnDraw(Canvas canvas)
        {
            base.OnDraw(canvas);

            initPoints();
            outerBox(canvas);
            innerBox(canvas);
            drawLine(canvas);

            drawCurvedLine(canvas);
            filledCurvedPath(canvas);
            drawText(canvas);
        }

        private Rect r = new Rect();
        private void drawText(Canvas canvas)
        {
            string label = "SPEED(MPH)";
            canvas.GetClipBounds(r);
            int cHeight = (int)box.Height();
            int cWidth = (int)box.Width();
            mPaint.Color = Color.Rgb(224, 228, 231);
            mPaint.TextSize = 15.0f * density;
            mPaint.TextAlign = Paint.Align.Left;
            mPaint.GetTextBounds(label, 0, label.Length, r);
            mPaint.SetTypeface(typeFace);
            float x = cWidth / 2f - r.Width() / 2f - r.Left;
            float y = cHeight * .10f;

            if (_isZoom)
            {
                mPaint.TextSize = 25.0f * density;
                x = cWidth *.05f;
                 y = cHeight * .15f;
            }
            canvas.DrawText(label, x, y, mPaint);


            string labelValue = "100";
            mPaint.TextSize = 35.0f * density;
            mPaint.TextAlign = Paint.Align.Left;
            mPaint.Color = Color.White;
            mPaint.GetTextBounds(labelValue, 0, labelValue.Length, r);
            float x2 = cWidth / 2f - r.Width() / 2f - r.Left;
            float y2 = cHeight * .20f + r.Height();

            if (_isZoom)
            {//TODO
                mPaint.TextSize = 90.0f * density;
                mPaint.GetTextBounds(labelValue, 0, labelValue.Length, r);
                x2 = cWidth*.90f- r.Width();
                y2 = cHeight * .10f+r.Height();
            }
            canvas.DrawText(labelValue, x2, y2, mPaint);

            string maxValue = "MAX: 154";
            mPaint.TextSize = 15.0f * density;
            mPaint.Color = Color.Rgb(224, 228, 231);
            mPaint.TextAlign = Paint.Align.Left;
            mPaint.GetTextBounds(maxValue, 0, maxValue.Length, r);
            float x3 = cWidth / 2f - r.Width() / 2f - r.Left;
            float y3 = box.Bottom * .98f;
            if (_isZoom)
            {
                mPaint.TextSize = 25.0f * density;
                x3 = cWidth * .05f;
                y3 = cHeight*.25f;
            }
            canvas.DrawText(maxValue, x3, y3, mPaint);

        }




        private void drawLine(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(255, 182, 66);
            mPaint.SetStyle(Paint.Style.Stroke);
            mPaint.StrokeWidth = 3f * density;
            canvas.DrawLine(lineX, lineY, box.Right, lineY, mPaint);

        }
        private void drawCurvedLine(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(255, 182, 66);
            mPaint.SetStyle(Paint.Style.Stroke);
            mPaint.StrokeWidth = 3f * density;
            mPaint.AntiAlias = true;
            mCurve.MoveTo(points[0].X, points[0].Y);

            for (int i = 1; i < points.Length; i++)
            {
                mCurve.CubicTo(pointsCon1[i].X, pointsCon1[i].Y, pointsCon2[i].X, pointsCon2[i].Y, points[i].X, points[i].Y);
            }

            canvas.DrawPath(mCurve, mPaint);
            mCurve.Reset();

        }
        private void filledCurvedPath(Canvas canvas)
        {
            mPaint.Color = Color.Rgb(87, 59, 14);
            mPaint.SetStyle(Paint.Style.Fill);
            mCurve.MoveTo(points[0].X, points[0].Y);
            for (int i = 1; i < points.Length; i++)
            {
                mCurve.CubicTo(pointsCon1[i].X, pointsCon1[i].Y, pointsCon2[i].X, pointsCon2[i].Y, points[i].X, points[i].Y);
            }
            canvas.DrawPath(mCurve, mPaint);
            mCurve.Reset();
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
            tPath.LineTo(box.Right, box.Bottom - (box.Right - box.Right * .90f));
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
