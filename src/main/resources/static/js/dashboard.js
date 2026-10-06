const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    resizeDelay: 100,
    plugins: {
        legend: {
            position: 'bottom',
            labels: {
                font: {
                    family: 'Inter',
                    size: 12
                },
                padding: 15
            }
        }
    },
    cutout: '65%'
};

const catsPorAreaCanvas = document.getElementById('catsPorAreaChart');
if (catsPorAreaCanvas) {
    new Chart(catsPorAreaCanvas, {
        type: 'doughnut',
        data: {
            labels: ['Elétrica', 'Civil'],
            datasets: [{
                data: [Number(catsPorAreaCanvas.dataset.eletrica), Number(catsPorAreaCanvas.dataset.civil)],
                backgroundColor: ['#0B3D91', '#FF6B35'],
                borderWidth: 0
            }]
        },
        options: doughnutOptions
    });
}

const itensPorAreaCanvas = document.getElementById('itensPorAreaChart');
if (itensPorAreaCanvas) {
    new Chart(itensPorAreaCanvas, {
        type: 'doughnut',
        data: {
            labels: ['Elétrica', 'Civil'],
            datasets: [{
                data: [Number(itensPorAreaCanvas.dataset.eletrica), Number(itensPorAreaCanvas.dataset.civil)],
                backgroundColor: ['#0B3D91', '#FF6B35'],
                borderWidth: 0
            }]
        },
        options: doughnutOptions
    });
}
