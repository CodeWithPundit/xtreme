# API Integration Guide

## Xtream Codes API
- Base URL: `{server_url}/player_api.php`
- Authentication: Username + Password
- Endpoints:
  - `?action=get_live_categories`
  - `?action=get_live_streams`
  - `?action=get_vod_categories`
  - `?action=get_vod_streams`
  - `?action=get_series_categories`
  - `?action=get_series`

## Stalker Portal API
- Handshake → Auth → Profile → Channels
- Token-based authentication
- Session cookie persistence
