#!/bin/bash

# ArgoCD setup script
set -e

echo "🔧 Setting up ArgoCD for guarantee application..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
fi

# Install ArgoCD (if not already installed)
echo "📦 Installing ArgoCD..."
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
echo "⏳ Waiting for ArgoCD to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

# Apply ArgoCD application
echo "📋 Applying ArgoCD application manifests..."
kubectl apply -f argocd/appproject.yaml
kubectl apply -f argocd/application.yaml

# Get ArgoCD admin password
echo "🔑 Getting ArgoCD admin password..."
ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)

echo "✅ ArgoCD setup completed!"
echo "🌐 Access ArgoCD UI:"
echo "   kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "   https://localhost:8080"
echo "👤 Login credentials:"
echo "   Username: admin"
echo "   Password: ${ARGOCD_PASSWORD}"