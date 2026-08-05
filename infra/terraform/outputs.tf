output "instance_id" {
  description = "ID of the TutorOps EC2 instance."
  value       = aws_instance.this.id
}

output "private_ip" {
  description = "Private IP address of the TutorOps EC2 instance."
  value       = aws_instance.this.private_ip
}

output "public_ip" {
  description = "Ephemeral public IPv4 address of the TutorOps EC2 instance. It changes after stop/start."
  value       = aws_instance.this.public_ip
}
