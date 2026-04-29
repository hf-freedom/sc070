import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Select, message, Descriptions, Card, Statistic, Row, Col, Progress } from 'antd';
import { ReloadOutlined, EyeOutlined } from '@ant-design/icons';
import api from '../api';

const Reports = () => {
  const [firmware, setFirmware] = useState([]);
  const [loading, setLoading] = useState(false);
  const [report, setReport] = useState(null);
  const [selectedFirmwareId, setSelectedFirmwareId] = useState(null);

  const fetchFirmware = async () => {
    try {
      const res = await api.get('/firmware');
      setFirmware(res.data);
    } catch (error) {
      message.error('获取固件列表失败');
    }
  };

  useEffect(() => {
    fetchFirmware();
  }, []);

  const generateReport = async (firmwareId) => {
    setLoading(true);
    try {
      const res = await api.get(`/reports/coverage/${firmwareId}`);
      setReport(res.data);
      setSelectedFirmwareId(firmwareId);
    } catch (error) {
      message.error('生成报表失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: '固件名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '版本号',
      dataIndex: 'version',
      key: 'version',
    },
    {
      title: '适用型号',
      dataIndex: 'supportedModels',
      key: 'supportedModels',
      render: (models) => (
        <Space>
          {models?.map(m => <Tag key={m}>{m}</Tag>)}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={status === 'PUBLISHED' ? 'green' : 'default'}>
          {status === 'PUBLISHED' ? '已发布' : status}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button
          type="link"
          onClick={() => generateReport(record.id)}
          loading={loading && selectedFirmwareId === record.id}
        >
          生成报表
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h3>选择固件生成覆盖率报表</h3>
      </div>

      <Table
        columns={columns}
        dataSource={firmware}
        rowKey="id"
        style={{ marginBottom: 32 }}
      />

      {report && (
        <div>
          <h3 style={{ marginBottom: 24 }}>覆盖率报表 - {report.version}</h3>
          
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col span={6}>
              <Card>
                <Statistic title="总设备数" value={report.totalDevices} />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic title="已升级" value={report.upgradedDevices} valueStyle={{ color: '#3f8600' }} />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic title="待升级" value={report.pendingDevices} valueStyle={{ color: '#1890ff' }} />
              </Card>
            </Col>
            <Col span={6}>
              <Card>
                <Statistic title="失败" value={report.failedDevices} valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
          </Row>

          <Card title="覆盖率进度" style={{ marginBottom: 24 }}>
            <Progress
              percent={(report.coverageRate * 100).toFixed(1)}
              status={report.coverageRate >= 0.8 ? 'success' : 'active'}
              size="large"
            />
            <div style={{ marginTop: 8 }}>
              覆盖率: {(report.coverageRate * 100).toFixed(1)}%
            </div>
          </Card>

          <Row gutter={16}>
            <Col span={12}>
              <Card title="按型号分布">
                <Table
                  dataSource={Object.entries(report.byModel || {}).map(([key, value]) => ({ model: key, count: value }))}
                  rowKey="model"
                  columns={[
                    { title: '型号', dataIndex: 'model', key: 'model' },
                    { title: '已升级数量', dataIndex: 'count', key: 'count' },
                  ]}
                  pagination={false}
                />
              </Card>
            </Col>
            <Col span={12}>
              <Card title="按区域分布">
                <Table
                  dataSource={Object.entries(report.byRegion || {}).map(([key, value]) => ({ region: key, count: value }))}
                  rowKey="region"
                  columns={[
                    { title: '区域', dataIndex: 'region', key: 'region' },
                    { title: '已升级数量', dataIndex: 'count', key: 'count' },
                  ]}
                  pagination={false}
                />
              </Card>
            </Col>
          </Row>
        </div>
      )}
    </div>
  );
};

export default Reports;
