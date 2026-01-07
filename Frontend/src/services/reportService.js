import httpTool from "../http-common";

const buildQueryParams = (from, to) => {
    // Si viene null o undefined, enviamos string vacío
    const f = from || ""; 
    const t = to || "";
    return `?from=${f}&to=${t}`;
};

const getActiveRents = (from, to) => {
    return httpTool.get(`/api/report/active-rents${buildQueryParams(from, to)}`);
}

const getLateClients = (from, to) => {
    return httpTool.get(`/api/report/late-clients${buildQueryParams(from, to)}`);
}

const getToolRanking = (from, to) => {
    return httpTool.get(`/api/report/ranking${buildQueryParams(from, to)}`);
}

export default {getActiveRents, getLateClients, getToolRanking};