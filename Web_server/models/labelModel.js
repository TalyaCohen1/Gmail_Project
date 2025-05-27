let idCounter = 0
const labels = []

const getAllLabels = (userId) => labels.filter(label => label.userId === userId)

const getLabel = (id, userId) => labels.find(a => a.id === id && a.userId === userId)

const createLabel = (name, userId) => {
     const newLabel = { id: ++idCounter, name, userId} 
     labels.push(newLabel) 
     return newLabel 
}

const updateLabel = (id, name, userId) => {
     const label = getLabel(id, userId) 
     if (!label) return null
     if (name) label.name = name
     if (label.name === '') label.name = 'No Name'
     return label 
}

const deleteLabel = (id, userId) => {
     const index = labels.findIndex(a => a.id === id && a.userId === userId)
     if (index === -1) return null
     return labels.splice(index, 1)[0] 
}

module.exports = {
    getAllLabels,
    getLabel, 
    createLabel, 
    updateLabel, 
    deleteLabel 
}