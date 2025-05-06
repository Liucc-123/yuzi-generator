import {
  getGeneratorVoByIdUsingGet,
  useGeneratorUsingPost,
} from '@/services/backend/generatorController';
import { useModel, useParams } from '@@/exports';
import { DownloadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Collapse,
  Divider,
  Form,
  Image,
  Input,
  message,
  Row,
  Space,
  Typography,
} from 'antd';
import { saveAs } from 'file-saver';
import React, { useEffect, useState } from 'react';
import { Link } from 'umi';

/**
 * 生成器使用页面
 * @constructor
 */
const GeneratorUsePage: React.FC = () => {
  const { id } = useParams();
  const [form] = Form.useForm();

  const [loading, setLoading] = useState<boolean>(false);
  const [data, setData] = useState<API.GeneratorVO>({});
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [downloading, setDownloading] = useState<boolean>(false);

  const models = data?.modelConfig?.models ?? [];

  /**
   * 加载数据
   */
  const loadData = async () => {
    if (!id) {
      return;
    }
    setLoading(true);
    try {
      const res = await getGeneratorVoByIdUsingGet({
        id,
      });
      setData(res.data || {});
    } catch (error: any) {
      message.error('获取数据失败，' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [id]);

  /**
   * 下载按钮
   */
  const downloadButton = data.distPath && currentUser && (
    <Button
      type="primary"
      icon={<DownloadOutlined />}
      loading={downloading}
      onClick={async () => {
        setDownloading(true);
        const values = form.getFieldsValue();

        // eslint-disable-next-line react-hooks/rules-of-hooks
        const blob = await useGeneratorUsingPost(
          {
            id: data.id,
            dataModel: values,
          },
          {
            responseType: 'blob',
          },
        );
        // 使用 file-saver 来保存文件
        const fullPath = data.distPath || '';
        saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
        setDownloading(false);
      }}
    >
      生成代码
    </Button>
  );

  return (
    <PageContainer title={<></>} loading={loading}>
      <Card>
        <Row justify="space-between" gutter={[32, 32]}>
          <Col flex="auto">
            <Space size="large" align="center">
              <Typography.Title level={4}>{data.name}</Typography.Title>
            </Space>
            <Typography.Paragraph>{data.description}</Typography.Paragraph>
            <Divider />
            <Form form={form}>
              {models.map((model, index) => {
                // 是分组
                if (model.groupKey) {
                  if (!model.models) {
                    return <></>;
                  }

                  return (
                    <Collapse
                      key={index}
                      style={{
                        marginBottom: 24,
                      }}
                      items={[
                        {
                          key: index,
                          label: model.groupName + '（分组）',
                          children: model.models.map((subModel, index) => {
                            return (
                              <Form.Item
                                key={index}
                                label={subModel.fieldName}
                                // @ts-ignore
                                name={[model.groupKey, subModel.fieldName]}
                              >
                                <Input placeholder={subModel.description} />
                              </Form.Item>
                            );
                          }),
                        },
                      ]}
                      bordered={false}
                      defaultActiveKey={[index]}
                    />
                  );
                }

                return (
                  <Form.Item key={index} label={model.fieldName} name={model.fieldName}>
                    <Input placeholder={model.description} />
                  </Form.Item>
                );
              })}
            </Form>

            <div style={{ marginBottom: 24 }} />
            <Space size="middle">
              {downloadButton}
              <Link to={`/generator/detail/${id}`}>
                <Button>查看详情</Button>
              </Link>
            </Space>
          </Col>
          <Col flex="320px">
            <Image src={data.picture} />
          </Col>
        </Row>
      </Card>
    </PageContainer>
  );
};

export default GeneratorUsePage;
