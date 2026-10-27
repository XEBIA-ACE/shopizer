resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge({
    Name        = "${var.cluster_name}-vpc"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  azs = slice(data.aws_availability_zones.available.names, 0, var.az_count)
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = merge({
    Name        = "${var.cluster_name}-igw"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_subnet" "public" {
  for_each = { for idx, cidr in var.public_subnet_cidrs : idx => cidr }

  vpc_id                  = aws_vpc.this.id
  cidr_block              = each.value
  availability_zone       = local.azs[tonumber(each.key) % length(local.azs)]
  map_public_ip_on_launch = true

  tags = merge({
    Name                        = "${var.cluster_name}-public-${each.key}"
    "kubernetes.io/role/elb"    = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    ManagedBy                   = "ace"
    Environment                 = var.environment
    Project                     = var.project_id
  }, var.tags)
}

resource "aws_subnet" "private" {
  for_each = { for idx, cidr in var.private_subnet_cidrs : idx => cidr }

  vpc_id            = aws_vpc.this.id
  cidr_block        = each.value
  availability_zone = local.azs[tonumber(each.key) % length(local.azs)]

  tags = merge({
    Name                                  = "${var.cluster_name}-private-${each.key}"
    "kubernetes.io/role/internal-elb"     = "1"
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    ManagedBy                             = "ace"
    Environment                           = var.environment
    Project                               = var.project_id
  }, var.tags)
}

resource "aws_eip" "nat" {
  vpc = true

  tags = merge({
    Name        = "${var.cluster_name}-nat-eip"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public["0"].id

  tags = merge({
    Name        = "${var.cluster_name}-nat"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)

  depends_on = [aws_internet_gateway.this]
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  tags = merge({
    Name        = "${var.cluster_name}-public-rt"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_route" "public_internet_access" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public" {
  for_each       = aws_subnet.public
  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.this.id

  tags = merge({
    Name        = "${var.cluster_name}-private-rt"
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_route" "private_nat_gateway" {
  route_table_id         = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.this.id
}

resource "aws_route_table_association" "private" {
  for_each       = aws_subnet.private
  subnet_id      = each.value.id
  route_table_id = aws_route_table.private.id
}