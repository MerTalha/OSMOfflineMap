package com.example.osm_offlinemapdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private IMapView mMapView;
    private static IMapController mMapController;
    private Polyline line;
    TimerTask markerTask;
    Marker marker;
    ToggleButton toggleButton;
    Button deleteAllbtn;
    Button deleteBtn;
    Button openFileBtn;
    ArrayList<Double> arrayLot;
    ArrayList<Double> arrayLat;
    ArrayList<Double> arrayDistance;
    List<Overlay> overlays;
    double a;

    ArrayList<Marker> markerList = new ArrayList<>();
    ArrayList<Polyline> polylineList = new ArrayList<>();

    private static final int REQUEST_CODE_PICK_DIRECTORY = 123;


    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OSMDroid konfigürasyonunu başlat
        Configuration.getInstance().load(getApplicationContext(), getPreferences(MODE_PRIVATE));


        toggleButton = findViewById(R.id.toggleButton);
        deleteAllbtn = findViewById(R.id.deleteAllBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        openFileBtn = findViewById(R.id.openFileBtn);
        mMapView = findViewById(R.id.mapView);
        arrayLot = new ArrayList<>();
        arrayLat = new ArrayList<>();
        arrayDistance = new ArrayList<>();

        ((MapView) mMapView).setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);

        ((MapView) mMapView).setBuiltInZoomControls(true);
        ((MapView) mMapView).setUseDataConnection(false);
        ((MapView) mMapView).setMultiTouchControls(true);

        // Set the size of the image of the map
        double maxLatitude = 85.0; // Northern Border
        double minLatitude = -85.0; // South Border
        double minLongitude = -180.0; // West Border
        double maxLongitude = 180.0; // East Border

        ((MapView) mMapView).setScrollableAreaLimitLatitude(maxLatitude, minLatitude, 0);
        ((MapView) mMapView).setScrollableAreaLimitLongitude(minLongitude, maxLongitude, 0);

        mMapController = mMapView.getController();

        MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());

        ((MapView) mMapView).setTileSource(TileSourceFactory.MAPNIK);
        ((MapView) mMapView).setMinZoomLevel(2.0);
        ((MapView) mMapView).setMaxZoomLevel(17.0);
        loadOfflineMaps();


        ((MapView) mMapView).setTileSource(TileSourceFactory.MAPNIK);

        // Offline harita dosyasını yükle ve harita görünümüne ekle

        /*XYTileSource mCustomTileSource = new XYTileSource("4uMaps", 1, 16, 256, ".png", null, "/storage/emulated/0/osmdroid/tiles/Mapnik");
        mProvider.setTileSource(mCustomTileSource);*/
        //TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());

        //((MapView) mMapView).getOverlays().add(mTilesOverlay);

        line = new Polyline();

        line.getPaint().setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        line.setColor(Color.RED);
        ((MapView) mMapView).getOverlayManager().add(line);

        ((MapView) mMapView).setOnTouchListener((view, motionEvent) -> {

            if (toggleButton.isChecked()){
                 Timer markerTimer = new Timer();
                // Create and set markers
                marker = new Marker((MapView) mMapView);
                marker.setInfoWindow(null);
                applyDraggableListener(marker, (MapView) mMapView);


                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Start timer
                    markerTask = new TimerTask() {
                        @Override
                        public void run() {
                            // Here you can add markers
                            int offsetY = -110; // Adjust according to device screen
                            GeoPoint touchedPoint = (GeoPoint) mMapView.getProjection().fromPixels((int) motionEvent.getX(), (int) motionEvent.getY() + offsetY);
                            marker.setPosition(touchedPoint);
                            line.addPoint(touchedPoint);
                            // Add marker to map
                            overlays = ((MapView) mMapView).getOverlays();
                            overlays.add(marker);
                            polylineList.add(line);
                            markerList.add(marker);


                            final StringBuilder msg = new StringBuilder();
                            final double lon = (touchedPoint.getLongitude() / 1E6) * 1000000;
                            final double lat = (touchedPoint.getLatitude() / 1E6) * 1000000;
                            msg.append("Lon: ");
                            msg.append(lon);
                            msg.append(" Lat: ");
                            msg.append(lat);
                            String s = lat + "---" + lon;

                            arrayLot.add(lon);
                            arrayLat.add(lat);
                            Log.d("tag", s);

                            if (arrayLot.size()>=2 && arrayLat.size()>= 2){
                                Log.d("tag Distance", String.valueOf(line.getDistance()));
                            }

                            if (arrayDistance.size() == 0){
                                arrayDistance.add(line.getDistance());
                            } else if (arrayDistance.size() == 1) {
                                arrayDistance.add(line.getDistance()-arrayDistance.get(arrayDistance.size()-1));
                                Log.d("tag Array Distance", String.valueOf(arrayDistance.get(arrayDistance.size()-1)));
                            } else if (arrayDistance.size() >1) {
                                a = 0;
                                for (int i = 0; arrayDistance.size()-1 >= i; i++){
                                    a += arrayDistance.get(i);
                                }
                                arrayDistance.add(line.getDistance() - a);
                                Log.d("tag Array Distance", String.valueOf(arrayDistance.get(arrayDistance.size()-1)));
                            }

                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show());
                            ((MapView) mMapView).invalidate();
                            // Update map
                            ((MapView) mMapView).postInvalidate();
                        }
                    };
                    // Add marker if no touch again within 500 milliseconds (half a second)
                    markerTimer.schedule(markerTask, 500);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // Cancel timer
                    markerTimer.cancel();
                }
            }
            return false;
        });
        mMapController.setZoom(15);
        GeoPoint geoPecs = new GeoPoint(39.927784, 32.822267);
        mMapController.setCenter(geoPecs);

        deleteAllbtn.setOnClickListener(view -> deleteAll());

        deleteBtn.setOnClickListener(view -> delete());

        openFileBtn.setOnClickListener(view -> pickMapFolder());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String mapFolderPath = uri.toString(); // Seçilen klasörün URI'si
                    loadMap(mapFolderPath);
                }
            }
        }
    }


    private void pickMapFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
    }

    private void loadOfflineMaps() {
        File mapParentFolder = new File(Environment.getExternalStorageDirectory(), "/storage/emulated/0/osmdroid/tiles/Mapnik/4uMaps");

        if (mapParentFolder.exists() && mapParentFolder.isDirectory()) {
            for (File zoomLevelFolder : mapParentFolder.listFiles()) {
                if (zoomLevelFolder.isDirectory()) {
                    for (File regionFolder : zoomLevelFolder.listFiles()) {
                        if (regionFolder.isDirectory()) {
                            File[] mapFiles = regionFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

                            if (mapFiles != null && mapFiles.length > 0) {
                                for (File mapFile : mapFiles) {
                                    XYTileSource offlineTileSource = new XYTileSource(
                                            regionFolder.getName(),
                                            1,
                                            15,
                                            256,
                                            ".png",
                                            new String[] {}
                                    );

                                    MapTileProviderBasic offlineTileProvider = new MapTileProviderBasic(getApplicationContext(), offlineTileSource);

                                    TilesOverlay offlineOverlay = new TilesOverlay(offlineTileProvider, this.getBaseContext());
                                    ((MapView) mMapView).getOverlays().add(offlineOverlay);
                                }
                            }
                        }
                    }
                }
            }
            ((MapView) mMapView).invalidate();
        }
    }




    //                                    ((MapView) mMapView).invalidate();
    private void loadMap(String mapFolderPath) {
        Uri treeUri = Uri.parse(mapFolderPath);
        DocumentFile baseMapFolder = DocumentFile.fromTreeUri(this, treeUri);

        if (baseMapFolder != null && baseMapFolder.isDirectory()) {
            DocumentFile[] mapFiles = baseMapFolder.listFiles();

            if (mapFiles != null && mapFiles.length > 0) {
                DocumentFile selectedMapFile = mapFiles[0];

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedMapFile.getUri());
                    if (inputStream != null) {
                        Bitmap mapBitmap = BitmapFactory.decodeStream(inputStream);
                        if (mapBitmap != null) {
                            MapTileProviderBasic mProvider = new MapTileProviderBasic(getApplicationContext());
                            TilesOverlay mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());

                            ((MapView) mMapView).getOverlays().add(mTilesOverlay);

                            //Drawable mapDrawable = new BitmapDrawable(getResources(), mapBitmap);

                            //((MapView) mMapView).setBackground(mapDrawable);

                            ((MapView) mMapView).invalidate();
                            Toast.makeText(this, "Harita başarıyla yüklendi.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Harita görüntülenemedi.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "InputStream alınamadı.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Harita yüklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Seçilen klasörde uygun harita dosyası bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Geçerli bir klasör seçilmedi.", Toast.LENGTH_SHORT).show();
        }
    }


    public void deleteAll(){
        line.getActualPoints().clear();
        while (overlays.size() !=1){
            overlays.remove(overlays.get(overlays.size()-1));
        }
        arrayDistance.clear();
        arrayLat.clear();
        arrayLot.clear();
        markerList.clear();
        polylineList.clear();
        ((MapView) mMapView).getOverlayManager().add(line);

        ((MapView) mMapView).postInvalidate();
    }
    public void delete(){
        if (line.getActualPoints().size() !=0){
            overlays.remove(overlays.get(overlays.size()-1));
            line.getActualPoints().remove(line.getActualPoints().size()-1);
            line.setPoints(new ArrayList<>(line.getActualPoints()));
            if (polylineList.size() != 0){
                polylineList.remove(line.getActualPoints().size());
                markerList.remove(line.getActualPoints().size());
            }
            ((MapView) mMapView).postInvalidate();
        }
    }

    public  void applyDraggableListener(Marker poiMarker, MapView mapView  ) {

        marker.setDraggable(true);
        poiMarker.setOnMarkerClickListener((marker, mapView1) -> {
            mapView1.invalidate();

            return true;
        });

        poiMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newGeoPoint = marker.getPosition();
                Log.d("yeni geopoint", String.valueOf(newGeoPoint));

                int markerListSize = markerList.size();
                int polylineListSize = polylineList.size();
                for (int i = 0; i < markerListSize; i++){
                    GeoPoint markerPosition = markerList.get(i).getPosition();
                    Log.d("Marker Konum", "Latitude: " + markerPosition.getLatitude() + ", Longitude: " + markerPosition.getLongitude());
                }
                Log.d("List Sizes", "Marker List Size: " + markerListSize + ", Polyline List Size: " + polylineListSize);

                int draggedMarkerIndex = markerList.indexOf(marker);
                if (draggedMarkerIndex < 0 || draggedMarkerIndex >= markerList.size()) {
                    return;
                }

                marker.setPosition(newGeoPoint);
                markerList.get(draggedMarkerIndex).setPosition(newGeoPoint);

                mapView.invalidate();

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                GeoPoint newGeoPoint = marker.getPosition();

                int draggedMarkerIndex = markerList.indexOf(marker);
                if (draggedMarkerIndex < 0 || draggedMarkerIndex >= markerList.size()) {
                    return;
                }
                for (int i = 0; i < polylineList.size(); i++) {
                    Polyline connectedPolyline = polylineList.get(i);
                    List<GeoPoint> points = connectedPolyline.getActualPoints();
                    if (polylineList.size() != 0){
                        points.set(draggedMarkerIndex, newGeoPoint);
                    }
                    connectedPolyline.setPoints(new ArrayList<>(points));
                }
            }
        });
    }
}