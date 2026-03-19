output "sender_email" {
  value = aws_ses_email_identity.this.email
}

output "sender_email_identity_arn" {
  value = aws_ses_email_identity.this.arn
}