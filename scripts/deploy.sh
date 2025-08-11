#!/bin/bash

# Deploy script for guarantee application
set -e

echo "🚀 Deploying guarantee application to Kubernetes..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
fi

# Apply Kubernetes manifests
echo "📋 Applying Kubernetes manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Wait for deployment to be ready
echo "⏳ Waiting for deployment to be ready..."
kubectl rollout status deployment/guarantee-app -n guarantee --timeout=300s

# Show deployment status
echo "📊 Deployment status:"
kubectl get pods -n guarantee
kubectl get svc -n guarantee
kubectl get ingress -n guarantee

echo "✅ Deployment completed successfully!"