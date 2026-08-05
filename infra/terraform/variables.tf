variable "aws_region" {
  description = "AWS region in which to create the instance."
  type        = string
  default     = "eu-west-1"
}

variable "aws_profile" {
  description = "Optional local AWS CLI profile. Leave null to use the AWS credential chain or AWS_PROFILE."
  type        = string
  default     = null
}

variable "container_image" {
  description = "Public GHCR ARM64 image to run, pinned by immutable manifest digest."
  type        = string
  default     = "ghcr.io/andyintheshell/tutorops/tutorops-api@sha256:63902928661ae5e1ee15bb9eb26ced69002650bf687ef728a991fcd9ce1144b7"

  validation {
    condition     = can(regex("^ghcr\\.io/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+@sha256:[0-9a-f]{64}$", var.container_image))
    error_message = "container_image must be a GHCR image pinned to a 64-character SHA-256 manifest digest."
  }
}

variable "api_ingress_cidrs" {
  description = "IPv4 CIDR blocks allowed to reach the API on port 8080. Restrict this to your IP for private testing when possible."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "name" {
  description = "Name applied to the instance and security group."
  type        = string
  default     = "tutorops"
}
