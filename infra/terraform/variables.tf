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
  description = "Public GHCR image to run. Update this to the latest ARM64 SHA tag after publishing."
  type        = string
  default     = "ghcr.io/andyintheshell/tutorops/tutorops-api:sha-7587dac9167de8b9545f467babe30b2d9e9b82cd"

  validation {
    condition     = can(regex("^ghcr\\.io/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+:sha-[0-9a-f]{40}$", var.container_image))
    error_message = "container_image must be an immutable GHCR image tagged with a 40-character commit SHA."
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
