package com.example.osm_offlinemapdemo;

import static android.hardware.SensorManager.getAltitude;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
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

    ArrayList<Double> arrayList;

    ArrayList<Double> arrayList1;

    private static final int PICK_DOCUMENT_REQUEST_CODE = 100;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);
        mMapView = (MapView) findViewById(R.id.mapView);
        arrayList = new ArrayList<>();
        arrayList1 = new ArrayList<>();

        ((MapView) mMapView).setBuiltInZoomControls(true);
        ((MapView) mMapView).setUseDataConnection(false);
        ((MapView) mMapView).setMultiTouchControls(true);

        // Haritanın görüntüleneceği sınırları belirle
        double maxLatitude = 85.0; // Kuzey sınırı
        double minLatitude = -85.0; // Güney sınırı
        double minLongitude = -180.0; // Batı sınırı
        double maxLongitude = 180.0; // Doğu sınırı

        ((MapView) mMapView).setScrollableAreaLimitLatitude(maxLatitude, minLatitude, 0);
        ((MapView) mMapView).setScrollableAreaLimitLongitude(minLongitude, maxLongitude, 0);

        mMapController = mMapView.getController();

        MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());

        ((MapView) mMapView).setTileSource(TileSourceFactory.MAPNIK);
        ((MapView) mMapView).setMinZoomLevel(2.0);
        ((MapView) mMapView).setMaxZoomLevel(17.0);

        XYTileSource mCustomTileSource = new XYTileSource("4uMaps", 1, 16, 256, ".png", null, "/storage/emulated/0/osmdroid/tiles/Mapnik");
        mProvider.setTileSource(mCustomTileSource);
        TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());

        ((MapView) mMapView).getOverlays().add(mTilesOverlay);

        ((MapView) mMapView).setOnTouchListener((view, motionEvent) -> {
            if (toggleButton.isChecked()){
                 Timer markerTimer = new Timer();

                // Marker oluştur ve ayarla
                Marker marker = new Marker((MapView) mMapView);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Timer'ı başlat
                    TimerTask markerTask = new TimerTask() {
                        @Override
                        public void run() {
                            // Burada marker ekleme işlemini yapabilirsiniz
                            int offsetY = -110; // İstediğiniz miktara göre ayarlayın
                            GeoPoint touchedPoint = (GeoPoint) mMapView.getProjection().fromPixels((int) motionEvent.getX(), (int) motionEvent.getY() + offsetY);
                            marker.setPosition(touchedPoint);

                            // Marker'ı haritaya ekle
                            List<Overlay> overlays = ((MapView) mMapView).getOverlays();
                            // Eski markerı kaldır

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
                            arrayList.add(lon);
                            arrayList1.add(lat);
                            Log.d("tag", String.valueOf(arrayList.get(arrayList.size()-1)));
                            i++;

                            if (arrayList.size()>=2 && arrayList1.size()>= 2){
                                double a = getDistanceBetweenPointsNew(arrayList.get(arrayList.size()-1),
                                        arrayList1.get(arrayList1.size()-1),
                                        arrayList.get(arrayList.size()-2),
                                        arrayList1.get(arrayList1.size() -2));
                                Log.d("tag mesafe", String.valueOf(a));
                            }

                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show());
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

    public static double getDistanceBetweenPointsNew(double latitude1, double longitude1, double latitude2, double longitude2) {
        /*double theta = longitude1 - longitude2;
        double distance = 60 * 1.1515 * (180/Math.PI) * Math.acos(
                Math.sin(latitude1 * (Math.PI/180)) * Math.sin(latitude2 * (Math.PI/180)) +
                        Math.cos(latitude1 * (Math.PI/180)) * Math.cos(latitude2 * (Math.PI/180)) *
                                Math.cos(theta * (Math.PI/180))
        );
        return distance * 1.609344;*/

        // distance between latitudes and longitudes
        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLon = Math.toRadians(longitude2 - longitude1);

        // convert to radians
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(latitude1) *
                        Math.cos(latitude2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;

    }

}