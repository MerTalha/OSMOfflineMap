package com.example.osm_offlinemapdemo;

import static android.hardware.SensorManager.getAltitude;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private IMapView mMapView;
    private static IMapController mMapController;

    protected int i;

    ToggleButton toggleButton;

    ArrayList<String> arrayList;

    private static final int PICK_DOCUMENT_REQUEST_CODE = 100;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);
        mMapView = (MapView) findViewById(R.id.mapView);
        ((MapView) mMapView).setBuiltInZoomControls(true);
        ((MapView) mMapView).setUseDataConnection(false);

        ((MapView) mMapView).setMultiTouchControls(true);


        mMapController = mMapView.getController();

        arrayList = new ArrayList<>();

        MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());

        ((MapView) mMapView).setTileSource(TileSourceFactory.MAPNIK);
        ((MapView) mMapView).setMinZoomLevel(2.0);
        ((MapView) mMapView).setMaxZoomLevel(17.0);

        XYTileSource mCustomTileSource = new XYTileSource("4uMaps", 1, 16, 256, ".png", null, "/storage/emulated/0/osmdroid/tiles/Mapnik");
        //XYTileSource mCustomTileSource = new XYTileSource("turkey", 8, 18, 256, ".mbtiles", null, "/storage/emulated/0/osmdroid/tiles/Mapnik");
        mProvider.setTileSource(mCustomTileSource);
        TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());

        ((MapView) mMapView).getOverlays().add(mTilesOverlay);

        ((MapView) mMapView).setOnTouchListener((view, motionEvent) -> {
            if (toggleButton.isChecked()){
                GeoPoint touchedPoint = (GeoPoint) mMapView.getProjection().fromPixels((int) motionEvent.getX(), (int) motionEvent.getY());
                 Timer markerTimer = new Timer();

                // Marker oluştur ve ayarla
                Marker marker = new Marker((MapView) mMapView);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Timer'ı başlat
                    TimerTask markerTask = new TimerTask() {
                        @Override
                        public void run() {
                            // Burada marker ekleme işlemini yapabilirsiniz
                            int offsetY = -100; // İstediğiniz miktara göre ayarlayın
                            GeoPoint touchedPoint = (GeoPoint) mMapView.getProjection().fromPixels((int) motionEvent.getX(), (int) motionEvent.getY() + offsetY);
                            marker.setPosition(touchedPoint);

                            // Marker'ı haritaya ekle
                            List<Overlay> overlays = ((MapView) mMapView).getOverlays();
                            // Eski markerı kaldır

                            /*Overlay lastOverlay = overlays.get(overlays.size() - 1);
                            if (lastOverlay instanceof Marker) {
                                overlays.remove(lastOverlay);
                            }*/

                            //overlays.clear(); // Eski markerları temizle
                            overlays.add(marker);

                            final StringBuilder msg = new StringBuilder();
                            final double lon = (touchedPoint.getLongitude() / 1E6) * 1000000;
                            final double lat = (touchedPoint.getLatitude() / 1E6) * 1000000;
                            final double alt = getAltitude((float) lon, (float) lat);
                            msg.append("Lon: ");
                            msg.append(lon);
                            msg.append(" Lat: ");
                            msg.append(lat);
                            msg.append(" Alt: ");
                            msg.append(alt);
                            String s = lat + "---" + lon;

                            i = 0;
                            arrayList.add(s);
                            Log.d("tag", arrayList.get(arrayList.size()-1));
                            i++;

                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                            });
                            ((MapView) mMapView).invalidate(); // Haritanın güncellenmesini

                            // Haritayı güncelle
                            ((MapView) mMapView).postInvalidate();
                        }
                    };
                    // 500 milisaniye (yarım saniye) içinde tekrar dokunma olmazsa, marker eklemesini yap
                    markerTimer.schedule(markerTask, 500);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // Timer'ı iptal et
                    markerTimer.cancel();
                }
            }
            return false;
        });

        mMapController.setZoom(15);
        GeoPoint geoPecs = new GeoPoint(39.927784, 32.822267);
        mMapController.setCenter(geoPecs);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedFileUri = data.getData();
                String filePath = selectedFileUri.getPath();

                // .osm dosyasını MapView'a yansıtmak için gerekli işlemleri yapabilirsiniz
                MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());

                XYTileSource mCustomTileSource = new XYTileSource("4uMaps", 10, 18, 256, ".osm", new String[] {}) {
                    @NonNull
                    @Override
                    public String getTileURLString(long pMapTileIndex) {
                        return filePath + "/" + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + ".osm";
                    }
                };

                mProvider.setTileSource(mCustomTileSource);

                TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());
                ((MapView) mMapView).getOverlays().add(mTilesOverlay);

                ((MapView) mMapView).invalidate(); // Haritanın güncellenmesini
            }
        }
    }

}