--
-- flood-traffic-platform 数据库初始化脚本（仅表结构，不含数据）
-- 用法：psql -d flood_traffic_db -f db/schema.sql
-- （先执行 createdb flood_traffic_db 建库）
--
-- PostgreSQL database dump
--

-- Dumped from database version 12.17
-- Dumped by pg_dump version 12.17

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: node_overflow_result; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.node_overflow_result (
    id bigint NOT NULL,
    task_id character varying(50) NOT NULL,
    node_id character varying(50) NOT NULL,
    time_series jsonb NOT NULL,
    inflow_series jsonb NOT NULL
);


ALTER TABLE public.node_overflow_result OWNER TO postgres;

--
-- Name: COLUMN node_overflow_result.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.node_overflow_result.node_id IS '管点编号，与前端Shapefile转换后的GeoJSON属性对应';


--
-- Name: COLUMN node_overflow_result.time_series; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.node_overflow_result.time_series IS '时间轴数组';


--
-- Name: COLUMN node_overflow_result.inflow_series; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.node_overflow_result.inflow_series IS '对应的溢流量数组';


--
-- Name: node_overflow_result_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.node_overflow_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.node_overflow_result_id_seq OWNER TO postgres;

--
-- Name: node_overflow_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.node_overflow_result_id_seq OWNED BY public.node_overflow_result.id;


--
-- Name: simulation_task; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.simulation_task (
    task_id character varying(50) NOT NULL,
    status character varying(20) NOT NULL,
    parameters jsonb,
    result_tif_path character varying(255),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    end_time timestamp without time zone
);


ALTER TABLE public.simulation_task OWNER TO postgres;

--
-- Name: COLUMN simulation_task.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.simulation_task.status IS '任务状态 running, completed, error';


--
-- Name: COLUMN simulation_task.parameters; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.simulation_task.parameters IS '前端传入的仿真综合参数';


--
-- Name: COLUMN simulation_task.result_tif_path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.simulation_task.result_tif_path IS 'WCA2D生成的TIF结果文件绝对或相对路径';


--
-- Name: traffic_simulation_task; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.traffic_simulation_task (
    id character varying(64) NOT NULL,
    task_name character varying(128),
    flood_task_id character varying(64),
    status character varying(20) DEFAULT 'PENDING'::character varying,
    parameters jsonb NOT NULL,
    result_path character varying(255),
    error_message text,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.traffic_simulation_task OWNER TO postgres;

--
-- Name: TABLE traffic_simulation_task; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.traffic_simulation_task IS 'SUMO 交通仿真任务记录表';


--
-- Name: node_overflow_result id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.node_overflow_result ALTER COLUMN id SET DEFAULT nextval('public.node_overflow_result_id_seq'::regclass);


--
-- Name: node_overflow_result node_overflow_result_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.node_overflow_result
    ADD CONSTRAINT node_overflow_result_pkey PRIMARY KEY (id);


--
-- Name: simulation_task simulation_task_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.simulation_task
    ADD CONSTRAINT simulation_task_pkey PRIMARY KEY (task_id);


--
-- Name: traffic_simulation_task traffic_simulation_task_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.traffic_simulation_task
    ADD CONSTRAINT traffic_simulation_task_pkey PRIMARY KEY (id);


--
-- Name: idx_task_node; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_task_node ON public.node_overflow_result USING btree (task_id, node_id);


--
-- Name: node_overflow_result fk_task; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.node_overflow_result
    ADD CONSTRAINT fk_task FOREIGN KEY (task_id) REFERENCES public.simulation_task(task_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

