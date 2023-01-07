# MoviesBeforeTv

![Screenshot_20230107_115627](https://user-images.githubusercontent.com/74452431/211145720-c3b51b3d-30ef-4940-99dc-9e071402cea0.png)
![Screenshot_20230107_115650](https://user-images.githubusercontent.com/74452431/211145721-9a0cf2be-3789-46ec-98ec-90e592a71bcb.png)

![Screenshot_20230107_115727](https://user-images.githubusercontent.com/74452431/211145726-d66c796d-4ff7-4b46-b85c-a2692ece35ed.png)
![Screenshot_20230107_115702](https://user-images.githubusercontent.com/74452431/211145725-682b3a8e-d6a9-495e-932e-cdad6f89d3d7.png)

![Screenshot_20230107_115740](https://user-images.githubusercontent.com/74452431/211145728-f8926463-1bac-4455-b487-400a931cdb52.png)
![Screenshot_20230107_115800](https://user-images.githubusercontent.com/74452431/211145731-bf3ec473-9486-4d30-a50c-05d0afa557d9.png)

![Screenshot_20230107_115813](https://user-images.githubusercontent.com/74452431/211145717-b692680a-3978-4a58-9f4f-403c9d9591b7.png)
![Screenshot_20230107_115823](https://user-images.githubusercontent.com/74452431/211145719-cf2d40f1-b4fa-48c7-a20e-adc9ff8cb19a.png)

![2022-12-08](https://user-images.githubusercontent.com/74452431/206514838-66d428b2-9fae-4209-b2fb-8691b55e2fd8.png)

![Screenshot_20221208_182045](https://user-images.githubusercontent.com/74452431/206514939-92c8fbbd-03cd-4c09-9a6e-43ebf5da4e0e.png)

![Screenshot_20221208_182141](https://user-images.githubusercontent.com/74452431/206514961-51db9955-5bf6-494f-a37a-96026a2b4522.png)
It is a work in progress 🚧.

There are some bugs, lots of refactoring and some features to add as well testing
but the main goal was to experiment with the leanback library and
building a scaleable and modularized app.

#Main Features:
- Cached home categories that update once a day, with inifinte scroll using a network api and a local db
- Movie info, including trailers and cast
- Person info
- Search screen with search history
- Favories that depend wether a user is logged on. All favorites for logged users are cached and synced through Firestore
- Accounts using google account through Firebase Auth
- Analytics and crash reports, if enabled by the app in the account screen
- Some offline functionality, all cached data is available. Whenever connectivity is required the appropriate screen notifies about it.

Feel free to download the apk in the main folder to test the app for yourself.
If you wish to debug it, further instructions on configuration with firebase and api keys
will be attached soon.

Go Android!
