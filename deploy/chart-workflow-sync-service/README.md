Some notes about your Helm chart and Kubernetes configuration.

Consider using a namespace other than default. It will probably be best for
each team to work within their own Namespaces

https://github.com/discoveryinc-cs/mcd-terraform-eks-config/tree/master/modules/root


Information on readiness and liveness probes can be found here:
https://medium.com/spire-labs/utilizing-kubernetes-liveness-and-readiness-probes-to-automatically-recover-from-failure-2fe0314f2b2e

Make sure your pod starts and restarts quickly

There are resource limits defined in the template but they're also now defined
in each namespace from the chart-team-namespaces repo. Consider
adjusting them as low as possible.

