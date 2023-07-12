class mapKotlin : AppCompatActivity {


    companion object{
        val TURKEY = LatLong(39.924413, 32.814961)
    }

    private lateinit var b: ActivityMainBinding;
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidGraphicFactory.createInstance(application)

        Configuration.getInstance().load(applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        mapView = findViewById(R.id.map)
        //map = findViewById(R.id.mapView)

        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result->
            result?.data?.data?.let { uri->
                openMap(uri)
            }
        }
        b.button.setOnClickListener{
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )
        }
    }

    fun openMap(uri: Uri){
        b.map.mapScaleBar.isVisible = true
        b.map.setBuiltInZoomControls(true)
        val cache = AndroidUtil.createTileCache(
            this,
            "mycache",
            b.map.model.displayModel.tileSize,
            1f,
            b.map.model.frameBufferModel.overdrawFactor
        )

        val stream = contentResolver.openInputStream(uri) as FileInputStream

        val mapStore = MapFile(stream)

        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            b.map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        renderLayer.setXmlRenderTheme(
            InternalRenderTheme.DEFAULT
        )

        b.map.layerManager.layers.add(renderLayer)

        b.map.setCenter(TURKEY)
        b.map.setZoomLevel(10)
    }
}