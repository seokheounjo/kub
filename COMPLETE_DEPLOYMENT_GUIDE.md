# 🚀 완전 처음부터 시작하는 쿠버네티스 GitOps 배포 가이드

이 가이드는 guarantee 애플리케이션 폴더만 있는 상황에서부터 시작하여, KIND를 이용한 로컬 쿠버네티스 클러스터 구성, 애플리케이션 배포, ArgoCD GitOps 파이프라인까지 모든 과정을 다룹니다.

## 📋 시작 조건

- **현재 상황**: guarantee/ 폴더만 존재 (Spring Boot 애플리케이션)
- **목표**: 완전한 쿠버네티스 GitOps 환경 구축
- **요구사항**: Docker Desktop 설치 및 실행 중

## 🛠️ 1단계: 필수 도구 다운로드 및 설치

### 1.1 KIND(Kubernetes in Docker) 다운로드
```bash
# Windows용 KIND 바이너리 다운로드
curl -Lo kind-windows-amd64.exe https://kind.sigs.k8s.io/dl/v0.20.0/kind-windows-amd64
```

또는 브라우저에서 직접 다운로드:
- URL: https://github.com/kubernetes-sigs/kind/releases/download/v0.20.0/kind-windows-amd64

### 1.2 kubectl 다운로드
```bash
# Windows용 kubectl 다운로드
curl -LO https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe
```

또는 브라우저에서 직접 다운로드:
- URL: https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe

### 1.3 다운로드된 파일 확인
프로젝트 루트 디렉토리에 다음 파일들이 있는지 확인:
```
your-project/
├── guarantee/              # 기존 Spring Boot 애플리케이션
├── kind-windows-amd64.exe   # 다운로드한 KIND
└── kubectl.exe             # 다운로드한 kubectl
```

**진행률: 10% 완료** ✅

---

## 🐳 2단계: Dockerfile 생성

### 2.1 guarantee 폴더에 Dockerfile 생성
`guarantee/Dockerfile` 파일을 생성합니다:

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

**진행률: 15% 완료** ✅

---

## 📁 3단계: 쿠버네티스 매니페스트 파일 생성

### 3.1 k8s 디렉토리 생성
```bash
mkdir k8s
```

### 3.2 namespace.yaml 생성
`k8s/namespace.yaml` 파일 생성:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: guarantee
  labels:
    name: guarantee
```

### 3.3 deployment.yaml 생성
`k8s/deployment.yaml` 파일 생성:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: guarantee-app
  namespace: guarantee
  labels:
    app: guarantee
spec:
  replicas: 3
  selector:
    matchLabels:
      app: guarantee
  template:
    metadata:
      labels:
        app: guarantee
    spec:
      containers:
      - name: guarantee
        image: procof/guarantee:latest
        imagePullPolicy: Never
        ports:
        - containerPort: 8080
        env:
        - name: APP_VERSION
          value: "0.0.2"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### 3.4 service.yaml 생성
`k8s/service.yaml` 파일 생성:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: guarantee-service
  namespace: guarantee
  labels:
    app: guarantee
spec:
  selector:
    app: guarantee
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
```

### 3.5 ingress.yaml 생성
`k8s/ingress.yaml` 파일 생성:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: guarantee-ingress
  namespace: guarantee
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: guarantee.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: guarantee-service
            port:
              number: 80
```

**진행률: 25% 완료** ✅

---

## 🎯 4단계: ArgoCD 설정 파일 생성

### 4.1 argocd 디렉토리 생성
```bash
mkdir argocd
```

### 4.2 appproject.yaml 생성
`argocd/appproject.yaml` 파일 생성:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: guarantee-project
  namespace: argocd
spec:
  description: Guarantee application project
  sourceRepos:
  - 'https://github.com/seokheounjo/kub'  # 실제 Git 저장소 URL로 변경 필요
  destinations:
  - namespace: guarantee
    server: https://kubernetes.default.svc
  clusterResourceWhitelist:
  - group: ''
    kind: Namespace
  namespaceResourceWhitelist:
  - group: ''
    kind: Service
  - group: ''
    kind: ConfigMap
  - group: 'apps'
    kind: Deployment
  - group: 'networking.k8s.io'
    kind: Ingress
```

### 4.3 application.yaml 생성
`argocd/application.yaml` 파일 생성:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: guarantee-app
  namespace: argocd
  finalizers:
    - resources-finalizer.argocd.argoproj.io
spec:
  project: default
  source:
    repoURL: https://github.com/seokheounjo/kub  # 실제 Git 저장소 URL로 변경 필요
    targetRevision: HEAD
    path: k8s
  destination:
    server: https://kubernetes.default.svc
    namespace: guarantee
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
```

### 4.4 최종 프로젝트 구조 확인
```
your-project/
├── guarantee/              # Spring Boot 애플리케이션
│   ├── Dockerfile         # 새로 생성
│   ├── build.gradle
│   └── src/
├── k8s/                   # 새로 생성된 디렉토리
│   ├── namespace.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── ingress.yaml
├── argocd/                # 새로 생성된 디렉토리
│   ├── appproject.yaml
│   └── application.yaml
├── kind-windows-amd64.exe  # 다운로드한 파일
└── kubectl.exe            # 다운로드한 파일
```

**진행률: 35% 완료** ✅

---

## ⚙️ 5단계: KIND 클러스터 생성

### 5.1 KIND 클러스터 생성
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

### 5.2 클러스터 상태 확인
```bash
./kubectl.exe cluster-info --context kind-local-k8s
```

**예상 출력:**
```
Kubernetes control plane is running at https://127.0.0.1:50840
CoreDNS is running at https://127.0.0.1:50840/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

**진행률: 45% 완료** ✅

---

## 🐳 6단계: 애플리케이션 Docker 이미지 빌드

### 6.1 Docker 이미지 빌드
```bash
cd guarantee
docker build -t procof/guarantee:latest .
```

**예상 출력:**
```
#0 building with "desktop-linux" instance using docker driver
#1 [internal] load build definition from Dockerfile
...
#15 exporting to image
#15 exporting layers 1.0s done
#15 naming to docker.io/procof/guarantee:latest done
BUILD SUCCESSFUL
```

### 6.2 KIND 클러스터에 이미지 로드
```bash
cd ..
./kind-windows-amd64.exe load docker-image procof/guarantee:latest --name local-k8s
```

**예상 출력:**
```
Image: "procof/guarantee:latest" with ID "sha256:..." not yet present on node "local-k8s-control-plane", loading...
```

**진행률: 60% 완료** ✅

---

## ☸️ 7단계: 쿠버네티스 리소스 배포

### 7.1 네임스페이스 생성
```bash
./kubectl.exe apply -f k8s/namespace.yaml
```

**예상 출력:**
```
namespace/guarantee created
```

### 7.2 Deployment 배포
```bash
./kubectl.exe apply -f k8s/deployment.yaml
```

**예상 출력:**
```
deployment.apps/guarantee-app created
```

### 7.3 Service 배포
```bash
./kubectl.exe apply -f k8s/service.yaml
```

**예상 출력:**
```
service/guarantee-service created
```

### 7.4 NGINX Ingress Controller 설치
```bash
./kubectl.exe apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

### 7.5 Ingress Controller 준비 대기
```bash
./kubectl.exe wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=90s
```

**예상 출력:**
```
pod/ingress-nginx-controller-69847c6dfc-88tsh condition met
```

### 7.6 Ingress 배포
```bash
./kubectl.exe apply -f k8s/ingress.yaml
```

**예상 출력:**
```
ingress.networking.k8s.io/guarantee-ingress created
```

### 7.7 배포 상태 확인 (2-3분 소요)
```bash
./kubectl.exe get pods -n guarantee
```

**최종 예상 출력:**
```
NAME                             READY   STATUS    RESTARTS   AGE
guarantee-app-65cd4d54bf-4s88n   1/1     Running   0          2m
guarantee-app-65cd4d54bf-745t4   1/1     Running   0          2m
guarantee-app-65cd4d54bf-scmfs   1/1     Running   0          2m
```

**진행률: 75% 완료** ✅

---

## 🚀 8단계: 애플리케이션 접근 확인

### 8.1 포트 포워딩 설정
```bash
./kubectl.exe port-forward -n guarantee svc/guarantee-service 8080:80
```

> **참고**: 이 명령어는 계속 실행되므로 새 터미널 창을 열어서 다음 단계를 진행하세요.

### 8.2 애플리케이션 접근 테스트
브라우저에서 `http://localhost:8080` 접근하여 guarantee 애플리케이션이 정상 작동하는지 확인합니다.

**진행률: 85% 완료** ✅

---

## 🎯 9단계: ArgoCD 설치 및 설정

### 9.1 ArgoCD 네임스페이스 생성
```bash
./kubectl.exe create namespace argocd
```

**예상 출력:**
```
namespace/argocd created
```

### 9.2 ArgoCD 설치
```bash
./kubectl.exe apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

**예상 출력:**
```
customresourcedefinition.apiextensions.k8s.io/applications.argoproj.io created
...
networkpolicy.networking.k8s.io/argocd-server-network-policy created
```

### 9.3 ArgoCD 서버 준비 대기 (2-3분 소요)
```bash
./kubectl.exe wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd
```

**예상 출력:**
```
deployment.apps/argocd-server condition met
```

### 9.4 ArgoCD 관리자 초기 비밀번호 조회
```bash
./kubectl.exe -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

**예상 출력 (랜덤 생성됨):**
```
9FoOMDcRc9r7f4a-
```

> 📝 **중요**: 이 비밀번호를 안전한 곳에 기록해 두세요!

### 9.5 ArgoCD UI 접근을 위한 포트 포워딩
새 터미널 창에서 실행:
```bash
./kubectl.exe port-forward svc/argocd-server -n argocd 8081:443
```

### 9.6 ArgoCD AppProject 생성
```bash
./kubectl.exe apply -f argocd/appproject.yaml
```

**예상 출력:**
```
appproject.argoproj.io/guarantee-project created
```

### 9.7 ArgoCD Application 생성
```bash
./kubectl.exe apply -f argocd/application.yaml
```

**예상 출력:**
```
application.argoproj.io/guarantee-app created
```

**진행률: 100% 완료** 🎉

---

## 🎯 최종 결과 및 접근 정보

### ✅ 구축 완료된 환경
1. **로컬 쿠버네티스 클러스터** (KIND)
2. **Spring Boot 애플리케이션** (guarantee)  
3. **Ingress Controller** (NGINX)
4. **GitOps 도구** (ArgoCD)

### 🌐 접근 정보

#### 애플리케이션 접근
- **URL**: `http://localhost:8080`
- **설명**: guarantee Spring Boot 애플리케이션

#### ArgoCD 웹 UI 접근
- **URL**: `https://localhost:8081`
- **사용자명**: `admin`
- **비밀번호**: `[위에서 조회한 비밀번호]`

> ⚠️ **주의**: ArgoCD UI는 HTTPS를 사용하므로 브라우저에서 보안 경고가 나타날 수 있습니다. '고급' → '계속 진행' 선택하세요.

---

## 🔧 문제 해결 가이드

### 문제 1: Pod가 ImagePullBackOff 상태
**원인**: imagePullPolicy가 설정되지 않음

**해결책**:
```bash
# deployment.yaml에 imagePullPolicy: Never가 있는지 확인
./kubectl.exe describe pod -n guarantee <pod-name>
```

### 문제 2: Pod가 Ready 상태가 되지 않음
**원인**: Health check 실패

**해결책**:
```bash
# Pod 로그 확인
./kubectl.exe logs -n guarantee <pod-name>

# Health check 경로가 '/' 인지 확인
```

### 문제 3: ArgoCD 접속 불가
**원인**: 포트 포워딩 문제

**해결책**:
```bash
# ArgoCD 서버 상태 확인
./kubectl.exe get pods -n argocd

# 포트 포워딩 재시작
./kubectl.exe port-forward svc/argocd-server -n argocd 8081:443
```

### 문제 4: Docker 빌드 실패
**원인**: Docker Desktop 미실행 또는 권한 문제

**해결책**:
```bash
# Docker Desktop이 실행 중인지 확인
docker version

# guarantee 폴더에서 빌드 실행하는지 확인
```

---

## 📚 학습한 주요 개념

1. **컨테이너화**: Docker를 이용한 애플리케이션 패키징
2. **오케스트레이션**: Kubernetes를 이용한 컨테이너 관리  
3. **Infrastructure as Code**: YAML을 이용한 인프라 정의
4. **GitOps**: Git을 통한 배포 자동화
5. **서비스 메시**: Ingress를 통한 트래픽 관리

---

## 🚀 다음 단계 제안

1. **Git 연동**: 실제 Git 저장소와 ArgoCD 연결
2. **모니터링**: Prometheus + Grafana 추가  
3. **로깅**: ELK Stack 구성
4. **보안**: RBAC 및 보안 정책 설정
5. **CI/CD**: GitHub Actions와 연동

---

**🎉 축하합니다!** 

guarantee 폴더만 있던 상황에서 완전한 쿠버네티스 GitOps 환경을 성공적으로 구축했습니다! 이제 프로덕션 레벨의 컨테이너 오케스트레이션과 지속적 배포 파이프라인을 경험해보세요.