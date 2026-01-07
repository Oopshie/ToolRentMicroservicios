import httpTool from "../http-common";

const getActiveRents = () => {
    return httpTool.get('/api/report/active-rents');
}

const getLateClients = () => {
    return httpTool.get('/api/report/late-clients');
}

const getToolRanking = () => {
    return httpTool.get('/api/report/ranking');
}

export default {getActiveRents, getLateClients, getToolRanking};