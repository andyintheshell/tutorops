# TutorOps EC2

Creates one `t4g.small` Amazon Linux 2023 ARM64 instance in `eu-west-1`.
The instance uses the first subnet in the default VPC, receives an ephemeral
public IPv4 address, exposes the API on port 8080, and requires IMDSv2. Docker
pulls the configured public GHCR image during first boot and runs it with the
`cloud-smoke` Spring profile.

Run from this directory:

```bash
terraform init
terraform plan
terraform apply
```

AWS credentials and an account with permission to create the VPC security group
and EC2 instance must already be configured. The supplied image tag predates the
ARM64 workflow change; update `container_image` to the new SHA tag after the
workflow publishes it. For narrower testing access, override
`api_ingress_cidrs` with your public IPv4 CIDR, for example
`["203.0.113.10/32"]`.

After apply, test the public endpoints with the Terraform output:

```bash
curl "http://$(terraform output -raw public_ip):8080/api/public/status"
curl "http://$(terraform output -raw public_ip):8080/actuator/health"
```

The public IPv4 address is not static and changes after a stop/start. AWS also
charges for public IPv4 usage, even without an Elastic IP.
