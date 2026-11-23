#requires -Version 5.1
<#
 apply_monitoring.ps1 (Agent 1) - them giam sat Prometheus + Grafana cho ban Docker demo.
 Chay o GOC repo, tren nhanh moi tao tu origin/master, cay lam viec sach:
   git fetch origin --prune
   git switch -c feature/agent1-monitoring --no-track origin/master
   & D:\AgentChange\Agent1\apply_monitoring.ps1
 Them 6 file moi (khong ghi de file da co) va sua 2 file:
   pom.xml                      + micrometer-registry-prometheus
   ...\config\SecurityConfig.java  + "/actuator/prometheus" trong PUBLIC_ENDPOINTS
 Endpoint so lieu mac dinh KHONG duoc mo (404); chi mo khi dung docker-compose.monitoring.yml.
#>
$ErrorActionPreference = 'Stop'

if (-not (Test-Path 'pom.xml') -or -not (Test-Path 'docker-compose.demo.yml')) {
  throw 'Chay o goc repo (can thay pom.xml va docker-compose.demo.yml).'
}
$dirty = @(git status --short --untracked-files=no)
if ($dirty.Count -gt 0) { throw ('Cay lam viec co thay doi chua commit: ' + ($dirty -join '; ')) }
if ((git branch --show-current) -eq 'master') { throw 'Dang o nhanh master. Tao nhanh moi truoc (xem huong dan dau file).' }

$root = (Get-Location).Path
$utf8 = New-Object System.Text.UTF8Encoding($false)
$files = [ordered]@{}

$files['monitoring/prometheus/prometheus.yml'] = @'
# Cau hinh Prometheus cho GMS (Agent 1). Thu thap so lieu cua backend moi 10 giay.
global:
  scrape_interval: 10s
  evaluation_interval: 10s

scrape_configs:
  - job_name: gms-backend
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["backend:8080"]
        labels:
          application: gym-management-system

  - job_name: prometheus
    static_configs:
      - targets: ["localhost:9090"]
'@

$files['monitoring/grafana/provisioning/datasources/prometheus.yml'] = @'
apiVersion: 1
datasources:
  - name: Prometheus
    uid: prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
'@

$files['monitoring/grafana/provisioning/dashboards/dashboards.yml'] = @'
apiVersion: 1
providers:
  - name: gms
    folder: GMS
    type: file
    disableDeletion: false
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
'@

$files['monitoring/grafana/dashboards/gms-overview.json'] = @'
{
  "uid": "gms-overview",
  "title": "GMS Tổng quan backend",
  "tags": [
    "gms"
  ],
  "timezone": "browser",
  "schemaVersion": 39,
  "version": 1,
  "editable": true,
  "refresh": "10s",
  "time": {
    "from": "now-30m",
    "to": "now"
  },
  "panels": [
    {
      "id": 1,
      "type": "stat",
      "title": "Backend",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 0,
        "y": 0,
        "w": 4,
        "h": 4
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "max(up{job=\"gms-backend\"})",
          "instant": true
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "none",
          "mappings": [
            {
              "type": "value",
              "options": {
                "1": {
                  "text": "UP",
                  "color": "green"
                },
                "0": {
                  "text": "DOWN",
                  "color": "red"
                }
              }
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "red",
                "value": null
              },
              {
                "color": "green",
                "value": 1
              }
            ]
          }
        },
        "overrides": []
      },
      "options": {
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "colorMode": "value",
        "graphMode": "none",
        "textMode": "auto"
      }
    },
    {
      "id": 2,
      "type": "stat",
      "title": "Yêu cầu mỗi giây",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 4,
        "y": 0,
        "w": 5,
        "h": 4
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"gms-backend\"}[1m]))",
          "instant": true
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "reqps",
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "decimals": 2
        },
        "overrides": []
      },
      "options": {
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "colorMode": "value",
        "graphMode": "none",
        "textMode": "auto"
      }
    },
    {
      "id": 3,
      "type": "stat",
      "title": "Lỗi 5xx mỗi giây",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 9,
        "y": 0,
        "w": 5,
        "h": 4
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"gms-backend\",status=~\"5..\"}[1m])) or vector(0)",
          "instant": true
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "reqps",
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 0.01
              }
            ]
          },
          "decimals": 3
        },
        "overrides": []
      },
      "options": {
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "colorMode": "value",
        "graphMode": "none",
        "textMode": "auto"
      }
    },
    {
      "id": 4,
      "type": "stat",
      "title": "Độ trễ p95",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 14,
        "y": 0,
        "w": 5,
        "h": 4
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job=\"gms-backend\"}[1m])))",
          "instant": true
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "s",
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "yellow",
                "value": 0.5
              },
              {
                "color": "red",
                "value": 1
              }
            ]
          },
          "decimals": 3
        },
        "overrides": []
      },
      "options": {
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "colorMode": "value",
        "graphMode": "none",
        "textMode": "auto"
      }
    },
    {
      "id": 5,
      "type": "stat",
      "title": "Heap đang dùng",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 19,
        "y": 0,
        "w": 5,
        "h": 4
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum(jvm_memory_used_bytes{job=\"gms-backend\",area=\"heap\"})",
          "instant": true
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "bytes",
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          }
        },
        "overrides": []
      },
      "options": {
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "colorMode": "value",
        "graphMode": "none",
        "textMode": "auto"
      }
    },
    {
      "id": 6,
      "type": "timeseries",
      "title": "Yêu cầu theo mã trạng thái",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 0,
        "y": 4,
        "w": 12,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum by (status) (rate(http_server_requests_seconds_count{job=\"gms-backend\"}[1m]))",
          "legendFormat": "{{status}}"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "reqps",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      },
      "description": "Số yêu cầu HTTP mỗi giây theo mã trả về (200, 401, 403, 500...)."
    },
    {
      "id": 7,
      "type": "timeseries",
      "title": "Độ trễ p50 và p95",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 12,
        "y": 4,
        "w": 12,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "histogram_quantile(0.50, sum by (le) (rate(http_server_requests_seconds_bucket{job=\"gms-backend\"}[1m])))",
          "legendFormat": "p50"
        },
        {
          "refId": "B",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job=\"gms-backend\"}[1m])))",
          "legendFormat": "p95"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "s",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      },
      "description": "Thời gian xử lý yêu cầu HTTP. Cần có lưu lượng thì mới có số."
    },
    {
      "id": 8,
      "type": "timeseries",
      "title": "Bộ nhớ JVM",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 0,
        "y": 12,
        "w": 8,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum by (area) (jvm_memory_used_bytes{job=\"gms-backend\"})",
          "legendFormat": "{{area}} đang dùng"
        },
        {
          "refId": "B",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "sum(jvm_memory_max_bytes{job=\"gms-backend\",area=\"heap\"})",
          "legendFormat": "heap tối đa"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "bytes",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      }
    },
    {
      "id": 9,
      "type": "timeseries",
      "title": "CPU",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 8,
        "y": 12,
        "w": 8,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "process_cpu_usage{job=\"gms-backend\"}",
          "legendFormat": "tiến trình"
        },
        {
          "refId": "B",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "system_cpu_usage{job=\"gms-backend\"}",
          "legendFormat": "toàn hệ thống"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "percentunit",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      }
    },
    {
      "id": 10,
      "type": "timeseries",
      "title": "Kết nối cơ sở dữ liệu (Hikari)",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 16,
        "y": 12,
        "w": 8,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "hikaricp_connections_active{job=\"gms-backend\"}",
          "legendFormat": "đang dùng"
        },
        {
          "refId": "B",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "hikaricp_connections_idle{job=\"gms-backend\"}",
          "legendFormat": "rảnh"
        },
        {
          "refId": "C",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "hikaricp_connections_max{job=\"gms-backend\"}",
          "legendFormat": "tối đa"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "none",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      }
    },
    {
      "id": 11,
      "type": "timeseries",
      "title": "Luồng JVM và GC",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 0,
        "y": 20,
        "w": 12,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "jvm_threads_live_threads{job=\"gms-backend\"}",
          "legendFormat": "luồng đang sống"
        },
        {
          "refId": "B",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "rate(jvm_gc_pause_seconds_count{job=\"gms-backend\"}[1m])",
          "legendFormat": "lần GC mỗi giây"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "none",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      }
    },
    {
      "id": 12,
      "type": "timeseries",
      "title": "Top 5 đường dẫn nhiều yêu cầu nhất",
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "gridPos": {
        "x": 12,
        "y": 20,
        "w": 12,
        "h": 8
      },
      "targets": [
        {
          "refId": "A",
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "expr": "topk(5, sum by (uri) (rate(http_server_requests_seconds_count{job=\"gms-backend\"}[5m])))",
          "legendFormat": "{{uri}}"
        }
      ],
      "fieldConfig": {
        "defaults": {
          "unit": "reqps",
          "custom": {
            "lineWidth": 1,
            "fillOpacity": 10,
            "showPoints": "never"
          }
        },
        "overrides": []
      },
      "options": {
        "legend": {
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "multi"
        }
      }
    }
  ],
  "templating": {
    "list": []
  },
  "annotations": {
    "list": []
  }
}
'@

$files['docker-compose.monitoring.yml'] = @'
# docker-compose.monitoring.yml (Agent 1). Giam sat backend bang Prometheus + Grafana. GHEP THEM vao ban demo, khong thay the:
#   docker compose -f docker-compose.demo.yml -f docker-compose.monitoring.yml up -d --build
# Backend chi lo so lieu /actuator/prometheus khi duoc bat o day (SPRING_APPLICATION_JSON); mac dinh (Jenkins, chay tay) KHONG lo.
services:
  backend:
    environment:
      SPRING_APPLICATION_JSON: '{"management":{"endpoints":{"web":{"exposure":{"include":"health,info,prometheus"}}},"metrics":{"tags":{"application":"gym-management-system"},"distribution":{"percentiles-histogram":{"http.server.requests":true}}}}}'

  prometheus:
    image: prom/prometheus:v2.53.0
    restart: unless-stopped
    depends_on:
      backend:
        condition: service_healthy
    command:
      - --config.file=/etc/prometheus/prometheus.yml
      - --storage.tsdb.path=/prometheus
      - --storage.tsdb.retention.time=7d
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    ports:
      - "${GMS_PROMETHEUS_PORT:-9090}:9090"

  grafana:
    image: grafana/grafana:11.1.0
    restart: unless-stopped
    depends_on:
      - prometheus
    environment:
      GF_SECURITY_ADMIN_USER: "admin"
      GF_SECURITY_ADMIN_PASSWORD: "${GMS_GRAFANA_PASSWORD:-admin12345}"
      # Demo: ai mo cung xem duoc dashboard (chi doc). Muon sua phai dang nhap admin.
      GF_AUTH_ANONYMOUS_ENABLED: "true"
      GF_AUTH_ANONYMOUS_ORG_ROLE: "Viewer"
      GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH: "/var/lib/grafana/dashboards/gms-overview.json"
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards:ro
      - grafana-data:/var/lib/grafana
    ports:
      - "${GMS_GRAFANA_PORT:-3001}:3000"

volumes:
  prometheus-data:
  grafana-data:
'@

$files['docs/MONITORING.md'] = @'
# Giám sát hệ thống bằng Prometheus và Grafana

Bản demo Docker có thể bật thêm giám sát: **Prometheus** thu thập số liệu của backend mỗi 10 giây, **Grafana** vẽ dashboard "GMS Tổng quan backend" có sẵn.

## Bật giám sát
```powershell
docker compose -f docker-compose.demo.yml -f docker-compose.monitoring.yml up -d --build
docker compose -f docker-compose.demo.yml -f docker-compose.monitoring.yml ps
```
Backend được tạo lại để lộ endpoint số liệu; các dịch vụ khác giữ nguyên. Mọi lệnh `docker compose` sau đó cũng phải kèm đủ hai file `-f`, nếu không Docker coi Prometheus và Grafana là "dịch vụ thừa".

| Địa chỉ | Dùng để |
|---|---|
| http://localhost:3001 | Grafana. Dashboard mở sẵn, xem không cần đăng nhập. Muốn sửa: `admin` / `admin12345` |
| http://localhost:9090 | Prometheus. Menu Status, Targets: `gms-backend` phải là `UP` |
| http://localhost:8082/actuator/prometheus | Số liệu thô của backend |

## Tạo lưu lượng để dashboard có số
Dashboard chỉ có số khi có người gọi API. Chạy vòng lặp này (có cả yêu cầu đúng và yêu cầu bị từ chối để thấy các mã 200 và 401):
```powershell
1..300 | ForEach-Object {
  Invoke-WebRequest http://localhost:8082/api/v1/packages -UseBasicParsing | Out-Null
  try { Invoke-WebRequest http://localhost:8082/api/v1/users -UseBasicParsing | Out-Null } catch { }
}
```
Hoặc chạy `.\scripts\demo-smoke.ps1` vài lần, hoặc thao tác trên giao diện web.

## Các ô trên dashboard
| Ô | Ý nghĩa |
|---|---|
| Backend | `UP` khi Prometheus lấy được số liệu |
| Yêu cầu mỗi giây, theo mã trạng thái | Lưu lượng HTTP, tách 200 / 401 / 403 / 500... |
| Lỗi 5xx mỗi giây | Lỗi phía máy chủ, đỏ khi vượt 0,01 lần mỗi giây |
| Độ trễ p50 và p95 | 50% và 95% yêu cầu được xử lý trong thời gian này |
| Bộ nhớ JVM, CPU, luồng và GC | Sức khoẻ của tiến trình Java |
| Kết nối cơ sở dữ liệu (Hikari) | Số kết nối đang dùng, rảnh và tối đa tới SQL Server |
| Top 5 đường dẫn | Những endpoint được gọi nhiều nhất |

## Bảo mật
- Endpoint `/actuator/prometheus` được cho phép không cần đăng nhập ở tầng bảo mật, **nhưng mặc định không được mở** (trả 404). Chỉ khi bật như trên (`SPRING_APPLICATION_JSON` trong `docker-compose.monitoring.yml`) nó mới có số liệu. Bản chạy Jenkins (cổng 8081) và chạy tay không bật nên không lộ số liệu.
- Số liệu gồm tên đường dẫn và thống kê JVM, không có dữ liệu người dùng hay mật khẩu. Nếu đưa lên môi trường thật, hãy chặn cổng này ở tầng mạng hoặc thêm xác thực.
- Đổi mật khẩu Grafana bằng biến `GMS_GRAFANA_PASSWORD` trước lệnh `up`. Cổng đổi bằng `GMS_GRAFANA_PORT`, `GMS_PROMETHEUS_PORT`.

## Xử lý sự cố
- Target `gms-backend` là `DOWN`: kiểm tra `http://localhost:8082/actuator/prometheus` trả 200. Trả 404 nghĩa là backend chưa được tạo lại với file giám sát, chạy lại lệnh `up` ở trên.
- Dashboard trống: chưa có lưu lượng, chạy vòng lặp ở trên rồi đợi 20 giây.
- Ô "Độ trễ" trống dù đã có lưu lượng: cần ít nhất một phút dữ liệu sau khi backend khởi động.
'@


foreach ($rel in $files.Keys) {
  if (Test-Path (Join-Path $root $rel)) { throw ("File da ton tai, dung lai: " + $rel) }
}

# ---- Sua pom.xml va SecurityConfig truoc (neu neo sai thi dung truoc khi ghi bat ky file moi nao)
$pom = Join-Path $root 'pom.xml'
$sec = Join-Path $root 'src\main\java\com\gym\management\config\SecurityConfig.java'

$p = [IO.File]::ReadAllText($pom, $utf8)
if ($p.Contains('micrometer-registry-prometheus')) { throw 'pom.xml da co micrometer-registry-prometheus, dung lai.' }
$pnl = "`n"; if ($p.Contains("`r`n")) { $pnl = "`r`n" }
$pAnchor = '      <artifactId>spring-boot-starter-actuator</artifactId>' + $pnl + '    </dependency>'
if (-not $p.Contains($pAnchor)) { throw 'Khong thay dong neo spring-boot-starter-actuator trong pom.xml' }
$pAdd = $pAnchor + $pnl + '    <dependency>' + $pnl + '      <groupId>io.micrometer</groupId>' + $pnl +
        '      <artifactId>micrometer-registry-prometheus</artifactId>' + $pnl + '      <scope>runtime</scope>' + $pnl + '    </dependency>'
$p = $p.Replace($pAnchor, $pAdd)

$s = [IO.File]::ReadAllText($sec, $utf8)
if ($s.Contains('/actuator/prometheus')) { throw 'SecurityConfig da co /actuator/prometheus, dung lai.' }
$snl = "`n"; if ($s.Contains("`r`n")) { $snl = "`r`n" }
$sAnchor = '"/actuator/health"' + $snl + '    };'
if (-not $s.Contains($sAnchor)) { throw 'Khong thay neo "/actuator/health" trong PUBLIC_ENDPOINTS' }
$sAdd = '"/actuator/health",' + $snl +
        '            // Số liệu cho Prometheus. Mặc định endpoint này KHÔNG được mở (trả 404); chỉ có khi đặt' + $snl +
        '            // management.endpoints.web.exposure.include có "prometheus" (xem docker-compose.monitoring.yml).' + $snl +
        '            "/actuator/prometheus"' + $snl + '    };'
$s = $s.Replace($sAnchor, $sAdd)

[IO.File]::WriteAllText($pom, $p, $utf8)
Write-Host '  ~ pom.xml' -ForegroundColor Green
[IO.File]::WriteAllText($sec, $s, $utf8)
Write-Host '  ~ SecurityConfig.java' -ForegroundColor Green

# ---- File moi (LF, UTF-8 khong BOM)
foreach ($rel in $files.Keys) {
  $path = Join-Path $root $rel
  $dir = Split-Path $path -Parent
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
  $text = ($files[$rel] -replace "`r`n", "`n") + "`n"
  [IO.File]::WriteAllText($path, $text, $utf8)
  Write-Host ("  + " + $rel) -ForegroundColor Green
}

Write-Host ''
Write-Host 'Xong. Kiem tra: git diff (pom.xml: them 5 dong, SecurityConfig: them 4 dong), git status --short (2 file M, 6 file moi).'
