package cufe.wby.cytux;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;
import com.guanaj.easyswipemenulibrary.State;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cufe.wby.cytux.Class.Music;
import cufe.wby.cytux.Reference.LoadMore;
import cufe.wby.cytux.Utils.BaseActivity;
import cufe.wby.cytux.Utils.MusicPlayer;
import cufe.wby.cytux.Utils.SoundPoolUtil;

public class Choosing extends BaseActivity {

    SoundPoolUtil sp = SoundPoolUtil.getInstance(this);

    public final static int LOAD_PER_QUERY = 10; //一次读取多少歌曲
    int pass = 100;

    private RecyclerView music_choose;
    private Adapter adapter;
    private Intent intent;
    private int cnt;
    ArrayList<Music> musicList = new ArrayList<>(); // 每次加载临时存储的歌曲全部信息（eg. 10个）

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        requestPermission(); // 动态请求读取音乐文件
        setTitle("Cytux     请点击选择歌曲");
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏


        //recyclerview
        music_choose = findViewById(R.id.music_choose);
        music_choose.setLayoutManager(new LinearLayoutManager(this));

        initAdapter();

    }

    /**
     * 暂停播放歌曲
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (intent == null)
            intent = new Intent(this, MusicPlayer.class);
        String action = MusicPlayer.ACTION_MUSIC_PAUSE;
        intent.setAction(action);
        startService(intent);
    }

    /**
     * 初始化Adapter
     */
    private void initAdapter() {
        musicList = loadMusics();
        adapter = new Adapter(R.layout.music_list, musicList);
        music_choose.setAdapter(adapter);

        //添加动画效果
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.isFirstOnly(false);
        adapter.setNotDoAnimationCount(4);

        //自动加载
        adapter.setOnLoadMoreListener(() -> music_choose.postDelayed(() -> {
            Log.i("LoadMore", "Now: " + cnt);
            musicList = loadMusics();
            if (cnt < 0) {
                for (int i = 0; i < -cnt; i++)
                    adapter.addData(musicList.get(i));
                Log.i("music_choose", "Newly Loaded " + -cnt);
                adapter.loadMoreEnd();
            } else {
                for (int i = 0; i < LOAD_PER_QUERY; i++)
                    adapter.addData(musicList.get(i));
                Log.i("music_choose", "Newly Loaded " + LOAD_PER_QUERY);
                adapter.loadMoreComplete();
            }
        }, 100), music_choose);
        adapter.setLoadMoreView(new LoadMore());
        adapter.setPreLoadNumber(2);
    }

    /**
     * 一次读取定义数量的歌曲
     */
    public ArrayList<Music> loadMusics() {
        int now = cnt, done = 0;
        ArrayList<Music> musicList = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(\\d+)(.*)");
        try (Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.AudioColumns.DISPLAY_NAME+" DESC")) {
            //这里是利用了数据库，不用从sd卡里过滤目录
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (name != null) {
                        if (pattern.matcher(name).matches()) continue; //由于数字开头的有许多缓存文件, 故跳过
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        Log.i("MusicList", name + " " + artist);
                        Log.i("MusicList", path);

                        if (!name.contains(".wav") && !name.contains(".mp3")) continue;
                        if (done++ < cnt) continue;
                        //获取专辑ID
                        int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        Music m = new Music(name, path, artist, albumId, duration, size);
                        //获取专辑ID
                        musicList.add(m);
                        now++;
                        if (now == LOAD_PER_QUERY + cnt) {
                            cnt = now;
                            Log.i("MusicList", "Loaded " + musicList.size());
                            break;
                        }
                    }
                }
                if (cursor.isAfterLast()) {
                    cnt = -now + cnt; //已扫描完毕
                }
            } else
                Log.e("MusicList", "NULL");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return musicList;
    }

    class Adapter extends BaseQuickAdapter<Music, BaseViewHolder> {

        public Adapter(@LayoutRes int layoutResId, @Nullable List<Music> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(final BaseViewHolder helper, Music item) {

            int position = helper.getAdapterPosition();

            helper.setText(R.id.name_and_duration, item.getName());
            helper.setImageBitmap(R.id.cover, getAlbumArt(item.getAlbumId()));
            helper.getView(R.id.start_config).setOnClickListener(v -> {
                SoundPoolUtil sp = Choosing.this.sp;
                sp.play(sp.ids[0]);
                Intent intent = new Intent(Choosing.this, Configurations.class);
                Music m = adapter.mData.get(position);
                intent.putExtra("Music", m);
                startActivity(intent);
            });
            helper.addOnClickListener(R.id.rc_main);
            helper.getView(R.id.rc_content).setOnClickListener(v -> {

                EasySwipeMenuLayout easySwipeMenuLayout = helper.getView(R.id.rc_main);
                open(easySwipeMenuLayout);
                SoundPoolUtil sp = Choosing.this.sp;
                sp.play(sp.ids[0]);

                Log.d("music_choose", "Clicked" + position);
                Intent intent = new Intent(Choosing.this, MusicPlayer.class);
                intent.setAction(MusicPlayer.ACTION_MUSIC_PLAY);
                intent.putExtra("path", adapter.mData.get(position).getPath());
                Choosing.this.startService(intent);
            });

        }
    }


    /**
     * 根据点击的位置打开相应的侧边栏
     */
    public void open(EasySwipeMenuLayout layout) {
        Method[] methods = layout.getClass().getDeclaredMethods();
        try {
            for (Method method : methods) {
                if (method.getName().equals("handlerSwipeMenu")) {
                    method.setAccessible(true);
                    method.invoke(layout, State.RIGHTOPEN);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求读写权限
     */
    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,

                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    /**
     * 根据专辑ID获取专辑封面图
     *
     * @param album_id 专辑ID
     */
    private Bitmap getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getBaseContext().getContentResolver().query(Uri.parse(mUriAlbums + "/" + album_id), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.cover_default);
        }
        return bm;
    }

}

