# radcheck
A web application that checks radiation readings in your local area and around the world. This app is live on the Internet right now at the following URL: [https://www.radcheck.app/](https://www.radcheck.app/)

## Description
This web application gives detailed reports of background radiation readings and pollution data. Users can define up to 20 locations to view daily updates of radiation measurements and air quality indexes.

## Languages and Technologies
The back-end of this application is made with Java OOP language along with Spring MVC framework. It uses JPA and Hibernate for persistent user accounts and API queries. HTML templates are parsed with Thymeleaf. HTML and CSS elements are styled with Bootstrap. Javascript and jQuery provide responsive functionality. Users are authenticated with secure logon via Spring Security. APIs used to build data include Safecast, AirVisual, Google Maps Javascript, Embed and Geocoding. GSON is implemented to convert JSON API results into Java objects.

## Main Features
* Report builder to deliver results on multiple locations inside a single web request. Reports can be viewed in a 2x2, 3x3 or 4x4 format.
* Have queries persist in the database for 21 hours, after which they can be updated with a new call to the API
* Prevents duplicate locations from existing in the database, and directs users to save those locations instead
* Error handling for null or outdated returns from APIs along with user input validation
* Implements an "Add to Home Screen" feature that allows for a more "native app" experience on mobile

[View the project tracker on Trello](https://trello.com/b/fRCs1igg/radcheck)

[View this project's wireframes on Moqups](https://app.moqups.com/lurchworld@gmail.com/93bZAuibtd/view)
