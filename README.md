# Nulab Challenge

Make a small application using our [Cacoo API](http://developer.nulab-inc.com/docs/cacoo/api/1/diagrams)
It is a free style. You can make any application as long as the API is used in the application.
However, please make sure to code server-side programs in java.

## Resources

- Backend
	- [Vert.x](http://vertx.io)
	- [Signpost](https://github.com/mttkay/signpost) for OAuth 1.0a
	
- Frontend
	- [Bootstrap](https://getbootstrap.com)

## How To Use
To clone and run this application, you'll need Git and 

1. 

You must update a file `etc/config.json` and provide with your Cacoo consumer key and secret.

2.
 
```
# Run the app
$ ./gradlew clean run

# Web should be accessible from http://localhost:8080/
```

## TODO

- Frontend
	- Pagination
	- Move diagram to folder
	- Remove confirmation dialog
	- Diagram details modal or screen
	- filters (sort on, sort type, etc)
	
- Backend
	- Unit test