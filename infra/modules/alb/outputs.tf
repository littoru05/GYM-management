output "alb_id" {
  value       = aws_lb.main.id
  description = "The ID of the Application Load Balancer"
}

output "alb_arn" {
  value       = aws_lb.main.arn
  description = "The ARN of the Application Load Balancer"
}

output "alb_dns_name" {
  value       = aws_lb.main.dns_name
  description = "The DNS name of the Application Load Balancer"
}

output "alb_zone_id" {
  value       = aws_lb.main.zone_id
  description = "The canonical hosted zone ID of the load balancer (useful for Route 53)"
}

output "backend_target_group_arn" {
  value       = aws_lb_target_group.backend.arn
  description = "The ARN of the Backend Target Group"
}

output "backend_target_group_name" {
  value       = aws_lb_target_group.backend.name
  description = "The name of the Backend Target Group"
}

output "frontend_target_group_arn" {
  value       = aws_lb_target_group.frontend.arn
  description = "The ARN of the Frontend Target Group"
}

output "frontend_target_group_name" {
  value       = aws_lb_target_group.frontend.name
  description = "The name of the Frontend Target Group"
}

output "http_listener_arn" {
  value       = aws_lb_listener.http.arn
  description = "The ARN of the HTTP listener"
}
