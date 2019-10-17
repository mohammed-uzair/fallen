# fallen
A library to detect falls, frequent falls and shakes in Android devices, store data in local database and display notifications. Also a public API
to get all the events from the database, you can get filter list of events as well.

I will support more enevt gestures in future.

# I am falling app
A sample android app that shows how to use fallen to detect falls and shakes and also frequent falls on an Android device.
Display a list with all the events. You can even filter the list with the event type.

### Implemented by Clean Architecture
The following diagram shows the structure of this project with 3 layers:
- View
- ViewModel
- Model

<br>
<p align="center">
  <img src="/documents/raw/mvvm.png"/>
</p>
<br>

### Communication between layers

1. UI calls method from ViewModel.
2. ViewModel initiates the sensors using the fallen library.
3. Fallen starts sensing data from the sensors and store events in the local database and displays notifications.
3. blaze-downloader saves the images usning LRU caching and spills the images to the adapter to render.
4. Apps gets all the events from the fallen public API.
5. Information flows back to the UI where we display the list of events, with a nice to have filter.

At a glance:

- Start sensing all fall events.
- Store the duration of events in the local database.
- Return all the events using the exposed public API.
- Display a list of all events with a live paged list with a filter to filter events(Shake/Falls).

![Example2](documents/raw/pagination.gif)

## How To Use

1. Register the fallen service in your application manifest
    <service android:name="com.example.uzair.fallen.events_service.EventDetectionService" />
    
2. Give the access of the application reference to the fallen library in the Application class
   public lateinit var fallen: Fallen

    override fun onCreate() {
        super.onCreate()

        fallen = Fallen(this)
    }
    
3. Start the fallen by 
        fallen.startFallen(
            detectFalls = detectFalls,
            detectShakes = detectShakes,
            detectFrequentFalls = detectFrequentFalls
        )
        
 Finally when you are over, stop the service
 fallen.stopFallen()
 
 4. You can also give some extra features to fallen like below
        fallen.setFallDetectionMessages(resources.getStringArray(R.array.fall_detected_messages))//String array of messages
        fallen.setFrequentFallDetectionMessages(resources.getStringArray(R.array.frequent_fall_messages))
        fallen.setShakeDetectionMessages(resources.getStringArray(R.array.shake_messages))
    
All Done :)

You can also provide fallen a list of messages 

I will be happy to add more updates frequently :)
