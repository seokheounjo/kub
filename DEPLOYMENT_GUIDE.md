# 쿠버네티스와 ArgoCD를 이용한 GitOps 배포 가이드

이 가이드는 KIND(Kubernetes in Docker)를 사용하여 로컬 쿠버네티스 클러스터를 구성하고, Spring Boot 애플리케이션을 배포한 후 ArgoCD를 통한 GitOps 파이프라인을 구축하는 전체 과정을 다룹니다.

## 📋 사전 요구사항

- Docker Desktop 설치 및 실행
- Windows 환경
- 프로젝트 구조:
  ```
  kub/
  ├── guarantee/          # Spring Boot 애플리케이션
  │   ├── Dockerfile
  │   ├── build.gradle
  │   └── src/
  ├── k8s/               # 쿠버네티스 매니페스트 파일들
  │   ├── namespace.yaml
  │   ├── deployment.yaml
  │   ├── service.yaml
  │   └── ingress.yaml
  ├── argocd/            # ArgoCD 설정 파일들
  │   ├── appproject.yaml
  │   └── application.yaml
  ├── kind-windows-amd64.exe  # KIND 바이너리
  └── kubectl.exe             # kubectl 바이너리
  ```

## 🚀 배포 과정

### 1단계: KIND 클러스터 생성

#### 1.1 KIND 클러스터 생성
```bash
./kind-windows-amd64.exe create cluster --name local-k8s
```

**예상 출력:**
```
Creating cluster "local-k8s" ...
 • Ensuring node image (kindest/node:v1.27.3) 🖼  ...
 ✓ Ensuring node image (kindest/node:v1.27.3) 🖼
 • Preparing nodes 📦   ...
 ✓ Preparing nodes 📦 
 • Writing configuration 📜  ...
 ✓ Writing configuration 📜
 • Starting control-plane 🕹️  ...
 ✓ Starting control-plane 🕹️
 • Installing CNI 🔌  ...
 ✓ Installing CNI 🔌
 • Installing StorageClass 💾  ...
 ✓ Installing StorageClass 💾
Set kubectl context to "kind-local-k8s"
```

#### 1.2 클러스터 상태 확인
```bash
./kubectl.exe cluster-info --context kind-local-k8s
```

**예상 출력:**
```
Kubernetes control plane is running at https://127.0.0.1:50840
CoreDNS is running at https://127.0.0.1:50840/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

**진행률: 20% 완료** ✅

---

### 2단계: 애플리케이션 Docker 이미지 빌드

#### 2.1 Dockerfile 확인
`guarantee/Dockerfile`의 내용을 확인합니다:
```dockerfile
# --- Build Stage ---
FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV APP_VERSION=0.0.2
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

#### 2.2 Docker 이미지 빌드
```bash
cd guarantee
docker build -t procof/guarantee:latest .
```

**예상 출력:**
```
#0 building with "desktop-linux" instance using docker driver
...
#15 exporting to image
#15 exporting layers 1.0s done
#15 naming to docker.io/procof/guarantee:latest done
```

#### 2.3 KIND 클러스터에 이미지 로드
```bash
./kind-windows-amd64.exe load docker-image procof/guarantee:latest --name local-k8s
```

**예상 출력:**
```
Image: "procof/guarantee:latest" with ID "sha256:..." not yet present on node "local-k8s-control-plane", loading...
```

**진행률: 40% 완료** ✅

---

### 3단계: 쿠버네티스 매니페스트 배포

#### 3.1 네임스페이스 생성
```bash
./kubectl.exe apply -f k8s/namespace.yaml
```

**예상 출력:**
```
namespace/guarantee created
```

#### 3.2 deployment.yaml 수정 (중요!)
KIND에서 로컬 이미지를 사용하기 위해 `k8s/deployment.yaml` 파일을 수정합니다:

**수정 전:**
```yaml
- name: guarantee
  image: procof/guarantee:0.0.2
  ports:
  - containerPort: 8080
```

**수정 후:**
```yaml
- name: guarantee
  image: procof/guarantee:latest
  imagePullPolicy: Never
  ports:
  - containerPort: 8080
```

또한 Health Check 경로도 수정합니다:

**수정 전:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
```

**수정 후:**
```yaml
livenessProbe:
  httpGet:
    path: /
    port: 8080
readinessProbe:
  httpGet:
    path: /
    port: 8080
```

#### 3.3 Deployment 배포
```bash
./kubectl.exe apply -f k8s/deployment.yaml
```

**예상 출력:**
```
deployment.apps/guarantee-app created
```

#### 3.4 Service 배포
```bash
./kubectl.exe apply -f k8s/service.yaml
```

**예상 출력:**
```
service/guarantee-service created
```

#### 3.5 NGINX Ingress Controller 설치
```bash
./kubectl.exe apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

#### 3.6 Ingress Controller 준비 대기
```bash
./kubectl.exe wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=90s
```

**예상 출력:**
```
pod/ingress-nginx-controller-69847c6dfc-88tsh condition met
```

#### 3.7 Ingress 배포
```bash
./kubectl.exe apply -f k8s/ingress.yaml
```

**예상 출력:**
```
ingress.networking.k8s.io/guarantee-ingress created
```

#### 3.8 배포 상태 확인
```bash
./kubectl.exe get all -n guarantee
```

**예상 출력 (모든 Pod가 Ready 상태가 될 때까지 2-3분 소요):**
```
NAME                             READY   STATUS    RESTARTS   AGE
pod/guarantee-app-65cd4d54bf-4s88n   1/1     Running   0          112s
pod/guarantee-app-65cd4d54bf-745t4   1/1     Running   0          81s
pod/guarantee-app-65cd4d54bf-scmfs   1/1     Running   0          2m23s

NAME                        TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
service/guarantee-service   ClusterIP   10.96.173.191   <none>        80/TCP    6m26s

NAME                            READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/guarantee-app   3/3     3            3           6m34s
```

**진행률: 70% 완료** ✅

---

### 4단계: 애플리케이션 접근 확인

#### 4.1 포트 포워딩 설정
```bash
./kubectl.exe port-forward -n guarantee svc/guarantee-service 8080:80 &
```

#### 4.2 애플리케이션 접근
브라우저에서 `http://localhost:8080` 접근하여 애플리케이션이 정상 작동하는지 확인합니다.

**진행률: 80% 완료** ✅

---

### 5단계: ArgoCD 설치 및 설정

#### 5.1 ArgoCD 네임스페이스 생성
```bash
./kubectl.exe create namespace argocd
```

**예상 출력:**
```
namespace/argocd created
```

#### 5.2 ArgoCD 설치
```bash
./kubectl.exe apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

**예상 출력:**
```
customresourcedefinition.apiextensions.k8s.io/applications.argoproj.io created
customresourcedefinition.apiextensions.k8s.io/applicationsets.argoproj.io created
...
networkpolicy.networking.k8s.io/argocd-server-network-policy created
```

#### 5.3 ArgoCD 서버 준비 대기
```bash
./kubectl.exe wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd
```

**예상 출력:**
```
deployment.apps/argocd-server condition met
```

#### 5.4 ArgoCD 관리자 초기 비밀번호 조회
```bash
./kubectl.exe -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

**예상 출력 (랜덤 생성됨):**
```
9FoOMDcRc9r7f4a-
```

> 📝 **중요**: 이 비밀번호를 안전한 곳에 기록해 두세요!

#### 5.5 ArgoCD UI 접근을 위한 포트 포워딩
```bash
./kubectl.exe port-forward svc/argocd-server -n argocd 8081:443 &
```

#### 5.6 ArgoCD AppProject 생성
```bash
./kubectl.exe apply -f argocd/appproject.yaml
```

**예상 출력:**
```
appproject.argoproj.io/guarantee-project created
```

#### 5.7 ArgoCD Application 생성
```bash
./kubectl.exe apply -f argocd/application.yaml
```

**예상 출력:**
```
application.argoproj.io/guarantee-app created
```

**진행률: 100% 완료** ✅

---

## 🎯 최종 접근 정보

### 애플리케이션 접근
- **URL**: `http://localhost:8080`
- **설명**: guarantee Spring Boot 애플리케이션

### ArgoCD 웹 UI 접근
- **URL**: `https://localhost:8081`
- **사용자명**: `admin`
- **비밀번호**: `9FoOMDcRc9r7f4a-` (실제 배포시 다를 수 있음)

> ⚠️ **주의**: ArgoCD UI는 HTTPS를 사용하므로 브라우저에서 보안 경고가 나타날 수 있습니다. '고급' → '계속 진행' 선택하세요.

## 🔧 문제 해결

### Pod가 ImagePullBackOff 상태인 경우
```bash
# deployment.yaml에 imagePullPolicy: Never 추가했는지 확인
./kubectl.exe describe pod -n guarantee <pod-name>
```

### Pod가 Ready 상태가 되지 않는 경우
```bash
# Pod 로그 확인
./kubectl.exe logs -n guarantee <pod-name>

# Health check 경로가 올바른지 확인 (/ 경로 사용)
```

### ArgoCD 접속 문제
```bash
# ArgoCD 서버 상태 확인
./kubectl.exe get pods -n argocd

# 포트 포워딩 재시작
./kubectl.exe port-forward svc/argocd-server -n argocd 8081:443
```

## 📚 주요 학습 포인트

1. **KIND 클러스터**: 로컬 개발을 위한 경량 쿠버네티스 환경
2. **Docker 멀티 스테이지 빌드**: 효율적인 이미지 생성
3. **쿠버네티스 리소스**: Namespace, Deployment, Service, Ingress
4. **Health Check**: Liveness/Readiness Probe 설정
5. **ArgoCD**: GitOps 기반 지속적 배포 도구

## 🚀 다음 단계

1. Git 저장소와 연동하여 실제 GitOps 워크플로우 구성
2. Helm 차트를 이용한 패키징
3. 모니터링 및 로깅 시스템 통합
4. 보안 정책 및 RBAC 설정

---

**배포 완료!** 🎉 이제 로컬 쿠버네티스 환경에서 GitOps 기반의 애플리케이션 배포 파이프라인이 구축되었습니다.