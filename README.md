# Nulab Challenge


## Goal
Make a small application using our [Cacoo API](http://developer.nulab-inc.com/docs/cacoo/api/1/diagrams)
It is a free style. You can make any application as long as the API is used in the application.
However, please make sure to code server-side programs in java.


## Result
Webapp where you can login with your Cacoo account and manage your diagrams. 

Consumed time: 12hs 

## Resources

- Backend
	- Java 8
	- [Vert.x](http://vertx.io)
	- [Signpost](https://github.com/mttkay/signpost) for OAuth 1.0a
	
- Frontend
	- [Bootstrap](https://getbootstrap.com)


## How To Use
To clone and run this application, you'll need Git and 

1. You must update a file `etc/config.json` with your Cacoo consumer key and secret.
2. Execute the command `./gradlew clean run` to run the app.
3. Visit http://localhost:8080/ to access the webapp.
 
 
## TODO

- Frontend
	- Pagination
	- Move diagram to folder
	- Remove confirmation dialog
	- Diagram details modal or screen
	- filters (sort on, sort type, etc)
	- Error page
	
- Backend
	- Move diagram to folder
	- Unit test
