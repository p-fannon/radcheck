document.getElementById('quickSearch').addEventListener('click', function() {
    console.log("Prompt fired");
    if ("geolocation" in navigator) {
        // check if geolocation is supported/enabled on current browser
        navigator.geolocation.getCurrentPosition(
            function success(position) {
             // for when getting location is a success
                document.getElementById('latitude').value = position.coords.latitude;
                document.getElementById('longitude').value = position.coords.longitude;
                console.log('latitude', position.coords.latitude, 'longitude', position.coords.longitude);
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