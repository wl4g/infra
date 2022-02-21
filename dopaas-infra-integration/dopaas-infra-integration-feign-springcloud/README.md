# Spring Cloud Feign Integration

A lightweight packaging component based on springboot + springcloud + feign microservice.


#### FAQ

- Q1: What roles do Gateway and Feign play in the Spring Cloud microservice architecture ?

> A1: The Gateway load balancer loads Web requests relative to the request load. Feign is the load between services, and the load between services can use flow limiting. Feign is not a service. The process is: the request goes to the Gateway, the Gateway routes the request to the corresponding service, if there is a call between the services, uses Feign to make the call between the services, and then the data is routed back to the Web


### Other 
- [Development trends of application layer microservice](https://www.cnblogs.com/y3blogs/p/13276504.html)