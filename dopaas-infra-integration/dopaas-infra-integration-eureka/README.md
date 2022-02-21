# SpringCloud Eureka Integrated Project

### Local mode 
| method | url | desc |  
| :--- |:---|:---|   
| GET | http://localhost:8761 | Register Center for singleton |  

- Starting

With `--spring.profiles.active=local` start eureka server. 

---


### HA Cluster mode 
- Configure the system hosts file: `C:\Windows\System32\drivers\etc\hosts` or `/etc/hosts`

```
127.0.0.1 peer1  peer2  peer3  localhost
```

| method | url | desc |  
| :--- |:---|:---|   
| GET | http://peer1:9001 | Register Center for peer1 |  
| GET | http://peer2:9002 | Register Center for peer2 | 
| GET | http://peer2:9003 | Register Center for peer3 |

- Starting

With `--spring.profiles.active=ha,peer1` and `--spring.profiles.active=ha,peer2` and `--spring.profiles.active=ha,peer3`   start Eureka server nodes in the same way. 