<<<<<<< HEAD
# Guarantee Application CI/CD

Spring Boot 기반 guarantee 서비스를 Docker + Kubernetes + ArgoCD로 구성한 완전한 CI/CD 파이프라인입니다.

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌──────────────────┐    ┌───────────────────┐
│   GitHub Repo   │───▶│  GitHub Actions  │───▶│  Docker Registry  │
└─────────────────┘    └──────────────────┘    └───────────────────┘
                                │                         │
                                ▼                         │
                       ┌──────────────────┐              │
                       │  Update K8s      │              │
                       │  Manifests       │              │
                       └──────────────────┘              │
                                │                         │
                                ▼                         │
                       ┌──────────────────┐              │
                       │     ArgoCD       │◀─────────────┘
                       │   (GitOps CD)    │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │   Kubernetes     │
                       │    Cluster       │
                       └──────────────────┘
```

## 📁 프로젝트 구조

```
kub/
├── guarantee/                 # Spring Boot 애플리케이션
│   ├── Dockerfile            # Docker 빌드 설정
│   ├── build.gradle          # Gradle 빌드 설정
│   └── src/                  # 소스 코드
├── k8s/                      # Kubernetes 매니페스트
│   ├── namespace.yaml        # 네임스페이스
│   ├── deployment.yaml       # 애플리케이션 배포
│   ├── service.yaml          # 서비스 노출
│   └── ingress.yaml          # 외부 접근
├── argocd/                   # ArgoCD 설정
│   ├── application.yaml      # ArgoCD 애플리케이션
│   └── appproject.yaml       # ArgoCD 프로젝트
├── scripts/                  # 배포 스크립트
│   ├── build.sh             # 빌드 스크립트
│   ├── deploy.sh            # 배포 스크립트
│   └── argocd-setup.sh      # ArgoCD 설정
└── .github/workflows/        # CI/CD 파이프라인
    └── ci-cd.yaml           # GitHub Actions
```

## 🚀 빠른 시작

### 1. 로컬 빌드 및 실행

```bash
# 애플리케이션 빌드
chmod +x scripts/build.sh
./scripts/build.sh 0.0.2

# 로컬 Docker 실행
docker run -p 8080:8080 guarantee:0.0.2
```

### 2. Kubernetes 배포

```bash
# Kubernetes에 배포
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 애플리케이션 접근
kubectl port-forward svc/guarantee-service -n guarantee 8080:80
```

### 3. ArgoCD 설정

```bash
# ArgoCD 설치 및 설정
chmod +x scripts/argocd-setup.sh
./scripts/argocd-setup.sh

# ArgoCD UI 접근
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

## 🔧 CI/CD 파이프라인

### GitHub Actions 워크플로우

1. **Test** - JUnit 테스트 실행
2. **Build & Push** - Docker 이미지 빌드 후 Registry 푸시
3. **Update Manifest** - Kubernetes 매니페스트 이미지 태그 업데이트

### ArgoCD GitOps

- Git 저장소의 변경사항을 자동으로 Kubernetes에 동기화
- 자동 복구(Self-Healing) 활성화
- 불필요한 리소스 자동 정리(Prune) 활성화

## 🛠️ 필요한 설정

### GitHub Secrets

CI/CD 파이프라인 실행을 위해 다음 시크릿 설정 필요:

```
DOCKER_USERNAME: Docker Hub 사용자명
DOCKER_PASSWORD: Docker Hub 패스워드
```

### Git 저장소 URL 업데이트

`argocd/application.yaml` 파일에서 실제 Git 저장소 URL로 변경:

```yaml
spec:
  source:
    repoURL: https://github.com/your-username/kub.git  # 실제 URL로 변경
```

## 📊 모니터링

### 애플리케이션 상태 확인

```bash
# Pod 상태
kubectl get pods -n guarantee

# 서비스 상태  
kubectl get svc -n guarantee

# 로그 확인
kubectl logs -f deployment/guarantee-app -n guarantee
```

### ArgoCD 대시보드

ArgoCD UI에서 애플리케이션 배포 상태와 동기화 히스토리를 확인할 수 있습니다.

## 🔗 접근 URL

- **애플리케이션**: `http://guarantee.local` (Ingress 설정 후)
- **ArgoCD**: `https://localhost:8080` (포트 포워딩 후)
- **H2 Console**: `http://localhost:8080/h2-console` (로컬 실행시)

## 📝 추가 정보

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.4
- **데이터베이스**: H2 (인메모리)
- **빌드 도구**: Gradle
- **컨테이너**: Docker + Kubernetes
- **CD 도구**: ArgoCD
=======
# kub
>>>>>>> f6a5e1179badfa35d78cf83c292cf6ed2f642139
