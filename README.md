# radcheck
A web app that checks radiation readings in your local area or around the world.

## Description
This web application gives detailed reports of background radiation readings. Users can define up to 16 locations to view daily updates of radiation measurements and air quality indexes.

## Languages and Technologies
The back-end of this application is made with Java OOP language along with Spring MVC framework. It uses JPA and Hibernate for persistent user accounts and API queries. HTML templates are parsed with Thymeleaf. HTML and CSS elements are styled with Bootstrap. Users are authenticated with secure logon via Spring Security. APIs used to build data include Google Maps Geocoding, Safecast and AirVisual. GSON is implemented to convert JSON API results into Java objects.

## Future Features
* Report builder to deliver results on multiple locations inside a single web request
* Have queries persist in the database for 21 hours, after which it can be updated with a new call to the API
* Prevent duplicate locations from existing in the database, and direct users to save those locations instead
* Error handling for null or outdated returns from APIs

[View the project tracker on Trello](https://trello.com/b/fRCs1igg/radcheck)

[View this project's wireframes on Moqups](https://app.moqups.com/lurchworld@gmail.com/93bZAuibtd/view)
