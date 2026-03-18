resource "aws_ses_email_identity" "this" {
  email = var.sender_email
}