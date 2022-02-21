# DoPaaS Infra for Canal Server

> This is an enhanced server integrating Alibaba canal.

## 1. Compile building

- Step1: First, building of [`canal`](https://github.com/alibaba/canal.git)

```bash
git clone https://github.com/alibaba/canal.git
cd canal
git checkout canal-1.1.5
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step2: Building of [`dopass-infra`](https://github.com/wl4g/dopass-infra)

```bash
# git clone https://github.com/wl4g/dopass-infra.git
cd dopass-infra
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

TODO
