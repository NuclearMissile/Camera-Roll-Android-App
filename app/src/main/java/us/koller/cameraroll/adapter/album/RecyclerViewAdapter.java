package us.koller.cameraroll.adapter.album;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import us.koller.cameraroll.R;
import us.koller.cameraroll.adapter.album.ViewHolder.AlbumItemHolder;
import us.koller.cameraroll.data.Album;
import us.koller.cameraroll.data.AlbumItem;
import us.koller.cameraroll.data.Gif;
import us.koller.cameraroll.data.Photo;
import us.koller.cameraroll.ui.ItemActivity;

public class RecyclerViewAdapter extends RecyclerView.Adapter {

    private int VIEW_TYPE_PHOTO = 1;
    private int VIEW_TYPE_GIF = 2;
    private int VIEW_TYPE_VIDEO = 3;

    private Album album;

    private boolean selector_mode = false;
    private boolean pick_photos;

    private boolean[] selected_items;

    private Callback callback;

    public RecyclerViewAdapter(Callback callback, Album album, boolean pick_photos) {
        this.callback = callback;
        this.album = album;
        this.pick_photos = pick_photos;
        if (pick_photos) {
            selector_mode = true;
            callback.onSelectorModeEnter();
        }
        selected_items = new boolean[album.getAlbumItems().size()];
    }

    @Override
    public int getItemViewType(int position) {
        AlbumItem albumItem = album.getAlbumItems().get(position);
        if (albumItem instanceof Photo) {
            return VIEW_TYPE_PHOTO;
        } else if (albumItem instanceof Gif) {
            return VIEW_TYPE_GIF;
        } else {
            return VIEW_TYPE_VIDEO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType == VIEW_TYPE_VIDEO ?
                R.layout.video_cover : R.layout.photo_cover, parent, false);
        return new AlbumItemHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final AlbumItem albumItem = album.getAlbumItems().get(position);
        ((AlbumItemHolder) holder).setAlbumItem(albumItem);
        holder.itemView.findViewById(R.id.image)
                .setSelected(selected_items[album.getAlbumItems().indexOf(albumItem)]);

        holder.itemView.setTag(albumItem.getPath());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (albumItem.error) {
                    return;
                }

                if (selector_mode) {
                    onItemSelected((AlbumItemHolder) holder);
                } else if (albumItem instanceof Photo || albumItem instanceof Gif) {
                    Intent intent = new Intent(holder.itemView.getContext(), ItemActivity.class);
                    intent.putExtra(ItemActivity.ALBUM_ITEM, albumItem);
                    intent.putExtra(ItemActivity.ALBUM, album);
                    intent.putExtra(ItemActivity.ITEM_POSITION, album.getAlbumItems().indexOf(albumItem));

                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    (Activity) holder.itemView.getContext(),
                                    holder.itemView.findViewById(R.id.image),
                                    albumItem.getPath());
                    holder.itemView.getContext().startActivity(intent, options.toBundle());
                } else {
                    File file = new File(albumItem.getPath());
                    Uri uri = FileProvider.getUriForFile(holder.itemView.getContext(),
                            holder.itemView.getContext().getApplicationContext().getPackageName() + ".provider", file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        holder.itemView.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(holder.itemView.getContext(), "No App found to play your video", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (((AlbumItemHolder) holder).albumItem.error) {
                    return true;
                }

                if (!selector_mode) {
                    selector_mode = true;
                    selected_items = new boolean[album.getAlbumItems().size()];
                    callback.onSelectorModeEnter();
                }

                onItemSelected((AlbumItemHolder) holder);
                return true;
            }
        });
    }

    public boolean isSelectorModeActive() {
        return selector_mode && !pick_photos;
    }

    private void checkForNoSelectedItems() {
        if (getSelectedItemCount() == 0 && !pick_photos) {
            selector_mode = false;
            cancelSelectorMode();
        }
    }

    private int getSelectedItemCount() {
        int k = 0;
        for (int i = 0; i < selected_items.length; i++) {
            if (selected_items[i]) {
                k++;
            }
        }
        return k;
    }

    private void onItemSelected(AlbumItemHolder holder) {
        selected_items[album.getAlbumItems().indexOf(holder.albumItem)]
                = !selected_items[album.getAlbumItems().indexOf(holder.albumItem)];
        holder.itemView.findViewById(R.id.image)
                .setSelected(selected_items[album.getAlbumItems().indexOf(holder.albumItem)]);
        callback.onItemSelected(getSelectedItemCount());
        checkForNoSelectedItems();
    }

    public AlbumItem[] cancelSelectorMode() {
        ArrayList<AlbumItem> selected_items = new ArrayList<>();
        selector_mode = false;
        for (int i = 0; i < this.selected_items.length; i++) {
            if (this.selected_items[i]) {
                notifyItemChanged(i);
                selected_items.add(album.getAlbumItems().get(i));
            }
        }
        this.selected_items = new boolean[album.getAlbumItems().size()];
        AlbumItem[] arr = new AlbumItem[selected_items.size()];
        callback.onSelectorModeExit();
        return selected_items.toArray(arr);
    }

    public boolean onBackPressed() {
        if (selector_mode && !pick_photos) {
            cancelSelectorMode();
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return album.getAlbumItems().size();
    }

    public static interface Callback {
        public void onSelectorModeEnter();

        public void onSelectorModeExit();

        public void onItemSelected(int selectedItemCount);
    }
}