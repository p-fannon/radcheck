document.getElementById('quickSearch').addEventListener('click', function() {
    console.log("Geolocation prompt fired");
    if ("geolocation" in navigator) {
        // check if geolocation is supported/enabled on current browser
        navigator.geolocation.getCurrentPosition(
            function success(position) {
             // for when getting location is a success
                let latitude = position.coords.latitude;
                let longitude = position.coords.longitude;
                document.getElementById('latitude').value = latitude;
                document.getElementById('longitude').value = longitude;
                console.log('latitude: ', latitude, ', longitude: ', longitude);
                var searchResult = new google.maps.LatLng(latitude, longitude);
                var mapOptions = {
                                 zoom: 17,
                                 center: searchResult,
                             }
                var newMap = new google.maps.Map(document.getElementById('map'), mapOptions);

                var marker = new google.maps.Marker({
                                        map: newMap,
                                        position: searchResult
                                    });
                document.getElementById('quickSearch').innerText = "Success! Please wait...";
                document.getElementById('locate').disabled = true;
                document.getElementById('quickSearch').disabled = true;
                document.getElementById("mapForm").submit();
             },
            function error(error_message) {
            // for when getting location results in an error
            alert('Geolocation was not successful for the following reason: ' + error_message);
            }
        );
    } else {
        // geolocation is not supported
        // get your location some other way
        alert('Geolocation is not enabled on this browser. Please use geocoding instead.');
    }
});