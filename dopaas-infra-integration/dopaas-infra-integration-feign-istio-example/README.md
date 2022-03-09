# DoPaaS Infra Integration Feign(Istio) Examples

> This example demonstrates how to use `dopaas-infra-integration-feign-istio` to quickly integrate feign based distributed architecture based on external integration locally.

## Quick start
- 1. [Start-up the Eureka service locally first](../dopaas-infra-integration-eureka/README.md)

- 2. Start-up the [FeginIstioExampleService](dopaas-infra-integration-feign-istio-example-starter-service/src/main/java/com/wl4g/FeginIstioExampleService.java)

- 3. Start-up the [FeginIstioExampleWeb](dopaas-infra-integration-feign-istio-example-starter-web/src/main/java/com/wl4g/FeginIstioExampleWeb.java)

- 4. for testing

  - Base URL.
  
  ```bash
  export BASE_WEB_URL='http://localhost:27001/istio-feign-example-web'
  ```

  - Query order list.

  ```bash
  curl "$BASE_WEB_URL/order/list?pageSize=2&pageNo=1"
  
  {"code":200,"status":"Normal","requestId":null,"message":"Ok","data":[{"orderNo":10001,"name":"Sniper rifle","deliveryAddress":"1458 Bee Street1","attributes":null},{"orderNo":10002,"name":"Over limit combat check","deliveryAddress":"95 Oxford Rd","attributes":null},{"orderNo":10003,"name":"fake vote","deliveryAddress":"394 Patterson Fork Road","attributes":null}]}
  ```

  - Create order.

  ```bash
  cat <<EOF>/tmp/1.json
  {
    "orderNo": 2021123124,
    "name": "jack121",
    "deliveryAddress":"china xxx 01",
    "remark": "aaabbccc12121"
  }
  EOF
  
  cat /tmp/1.json | curl -H "Content-Type:application/json" -X POST -d @- "$BASE_WEB_URL/order/createOrder"
  
  {"code":200,"status":"Normal","requestId":null,"message":"Ok","data":{}}
  ```

## FAQ

- 1. How to view the data of H2 database?

![Login h2](shots/login_h2.png)
![Select table](shots/select_table.png)
