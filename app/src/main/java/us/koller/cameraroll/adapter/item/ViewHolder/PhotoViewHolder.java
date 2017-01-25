package us.koller.cameraroll.adapter.item.ViewHolder;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import us.koller.cameraroll.R;
import us.koller.cameraroll.data.AlbumItem;
import us.koller.cameraroll.data.Photo;
import us.koller.cameraroll.ui.ItemActivity;
import us.koller.cameraroll.util.ViewUtil;

public class PhotoViewHolder extends ViewHolder {

    public PhotoViewHolder(AlbumItem albumItem, int position) {
        super(albumItem, position);
    }

    @Override
    public View getView(ViewGroup container) {
        ViewGroup v = super.inflateView(container);
        final View view = v.findViewById(R.id.subsampling);
        super.setOnClickListener(view);
        final View transitionView = itemView.findViewById(R.id.image);

        ViewUtil.bindTransitionView((ImageView) transitionView, albumItem);
        if (albumItem.isSharedElement) {
            view.setVisibility(View.INVISIBLE);
        } else {
            transitionView.setVisibility(View.INVISIBLE);
            ViewUtil.bindSubsamplingImageView(
                    (SubsamplingScaleImageView) view,
                    (Photo) albumItem, transitionView);
        }
        return v;
    }

    public void swapView(final boolean isReturning) {
        final View view = itemView.findViewById(R.id.subsampling);
        final View transitionView = itemView.findViewById(R.id.image);
        if (!isReturning) {
            view.setVisibility(View.VISIBLE);
            ViewUtil.bindSubsamplingImageView(
                    (SubsamplingScaleImageView) view,
                    (Photo) albumItem, transitionView);
        } else {
            view.setVisibility(View.INVISIBLE);
            transitionView.setVisibility(View.VISIBLE);
        }
    }

    public void scaleDown(final ItemActivity.Callback callback) {
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) itemView.findViewById(R.id.subsampling);
        if (imageView != null) {
            try {
                imageView.animateScale(0.0f)
                        .withDuration(300)
                        .withOnAnimationEventListener(new SubsamplingScaleImageView.DefaultOnAnimationEventListener() {
                            @Override
                            public void onComplete() {
                                super.onComplete();
                                swapView(true);
                                callback.callback();
                            }
                        })
                        .start();
            } catch (NullPointerException e) {
                swapView(true);
                callback.callback();
            }
        }
    }
}