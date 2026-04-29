import React, { useState } from 'react';
import { Layout, Menu, theme } from 'antd';
import {
  DesktopOutlined,
  AppstoreOutlined,
  CloudUploadOutlined,
  HistoryOutlined,
  ReloadOutlined,
  BarChartOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Devices from './pages/Devices';
import Groups from './pages/Groups';
import Firmware from './pages/Firmware';
import Plans from './pages/Plans';
import Tasks from './pages/Tasks';
import Rollbacks from './pages/Rollbacks';
import Reports from './pages/Reports';
import Logs from './pages/Logs';

const { Header, Sider, Content } = Layout;

const App = () => {
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const menuItems = [
    {
      key: '/',
      icon: <DesktopOutlined />,
      label: <Link to="/">设备管理</Link>,
    },
    {
      key: '/groups',
      icon: <AppstoreOutlined />,
      label: <Link to="/groups">设备分组</Link>,
    },
    {
      key: '/firmware',
      icon: <CloudUploadOutlined />,
      label: <Link to="/firmware">固件管理</Link>,
    },
    {
      key: '/plans',
      icon: <HistoryOutlined />,
      label: <Link to="/plans">升级计划</Link>,
    },
    {
      key: '/tasks',
      icon: <BarChartOutlined />,
      label: <Link to="/tasks">升级任务</Link>,
    },
    {
      key: '/rollbacks',
      icon: <ReloadOutlined />,
      label: <Link to="/rollbacks">回滚任务</Link>,
    },
    {
      key: '/reports',
      icon: <FileTextOutlined />,
      label: <Link to="/reports">覆盖率报表</Link>,
    },
  ];

  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
          <div
            style={{
              height: 32,
              margin: 16,
              background: 'rgba(255, 255, 255, 0.2)',
              borderRadius: 6,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
              fontWeight: 'bold',
            }}
          >
            {collapsed ? 'IoT' : 'IoT升级系统'}
          </div>
          <Menu
            theme="dark"
            defaultSelectedKeys={['/']}
            mode="inline"
            items={menuItems}
          />
        </Sider>
        <Layout>
          <Header
            style={{
              padding: '0 24px',
              background: colorBgContainer,
              display: 'flex',
              alignItems: 'center',
              fontSize: '18px',
              fontWeight: 'bold',
            }}
          >
            IoT 设备固件升级管理系统
          </Header>
          <Content
            style={{
              margin: '24px 16px',
              padding: 24,
              minHeight: 280,
              background: colorBgContainer,
              borderRadius: borderRadiusLG,
            }}
          >
            <Routes>
              <Route path="/" element={<Devices />} />
              <Route path="/groups" element={<Groups />} />
              <Route path="/firmware" element={<Firmware />} />
              <Route path="/plans" element={<Plans />} />
              <Route path="/tasks" element={<Tasks />} />
              <Route path="/rollbacks" element={<Rollbacks />} />
              <Route path="/reports" element={<Reports />} />
              <Route path="/logs/:taskId" element={<Logs />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </BrowserRouter>
  );
};

export default App;
